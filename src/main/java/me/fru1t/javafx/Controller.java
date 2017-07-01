package me.fru1t.javafx;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.Getter;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles creation and destroying of FXML resource stages and scenes. Each FXML file should have
 * a corresponding Controller class that handles the resource file's events and interactions.
 * Within the FXML file, the root element should have the <code>fx:controller</code> attribute
 * defined like so:
 * <pre>
 *     &lt;RootElement attribute1="value1"
 *                  fx:controller="fully.qualified.name.to.my.controller"&gt;
 *          ...
 *     &lt;/RootElement&gt;
 * </pre>
 *
 * The controller class MUST be annotated with the {@link FXMLResource} annotation in order
 * for this class to work properly:
 * <pre>
 *     &#64;FXMLResource("path_to.fxml")
 *     public class MyClass extends Controller { ... }
 * </pre>
 *
 * A controller's lifecycle is as follows:
 * <ol>
 *     <li>{@link #create(Class)} or any derivative #create method is called which
 *     triggers the FXMLLoader to instantiate a Controller object.</li>
 *     <li>{@link #onSceneCreate()} is immediately called. Here, all
 *     {@link javafx.fxml.FXML} annotated fields within the controller are already populated.</li>
 *     <li>{@link #onStageProvide(Stage)} is eventually called. Note that this call
 *     may not immediately proceed the #onSceneCreate call as the implementor may wait to give
 *     a stage to the controller. A call to {@link #create(Class, Stage)} will
 *     guarantee that #onStageProvide follows immediately after #onSceneCreate.</li>
 *     <li>At this point, the controller is done setting up and normal usage may be assumed.</li>
 *     <li>{@link #onShutdown()} is eventually called.</li>
 * </ol>
 */
public abstract class Controller {
    private static final Logger LOGGER = Logger.getLogger(Controller.class.getName());

    /**
     * Creates a new instance of an FXML layout returning the controller that controls it.
     * @param controllerClass The controller class for the FXML layout.
     * @param <T> The controller class for the FXML layout.
     * @return The controller to the new layout instance.
     */
    public static <T extends Controller> T create(Class<T> controllerClass) {
        // Get spec annotation
        FXMLResource specAnnotation = null;
        for (Annotation annotation : controllerClass.getAnnotations()) {
            if (annotation instanceof FXMLResource) {
                specAnnotation = (FXMLResource) annotation;
                break;
            }
        }
        if (specAnnotation == null) {
            // This is a implementation error. We shouldn't gracefully handle it.
            throw new RuntimeException(controllerClass.toString() + " requires the " +
                    FXMLResource.class.toString() + " annotation.");
        }

        // Get resource path
        URL resourceUrl = controllerClass.getResource(specAnnotation.value());
        if (resourceUrl == null) {
            // This is an implementation error. We shouldn't gracefully handle it.
            throw new RuntimeException("Couldn't find the FXML file '"
                    + specAnnotation.value() + "' from controller "
                    + controllerClass.toString());
        }

        // Load the fxml
        FXMLLoader loader = new FXMLLoader(resourceUrl);
        Parent fxmlRoot;
        try {
            fxmlRoot = loader.load();
        } catch (IOException e) {
            // We erred while opening a valid URL. This is indicative of a larger problem that's
            // unrelated to us.
            throw new RuntimeException(e);
        }

        // Pass the root parent to the controller through a scene.
        T controller = loader.getController();
        if (controller == null) {
            throw new RuntimeException("FXML file " + specAnnotation.value() + " has no "
                    + "fx:controller defined in the root element.");
        }
        controller.scene = new Scene(fxmlRoot);
        controller.onSceneCreate();

        return controller;
    }

    /**
     * Creates and provides a new instance of an FXML layout.
     * @param controllerClass The controller to create.
     * @param stage The stage to hand the controller.
     * @param <T> The type of controller.
     * @return The controller object.
     * @see #create(Class)
     */
    public static <T extends Controller> T create(Class<T> controllerClass, Stage stage) {
        T controller = create(controllerClass);
        controller.onStageProvide(stage);
        return controller;
    }

    /**
     * Creates a new instance of an FXML layout passing it a new stage.
     * @param controllerClass The controller class to create.
     * @param <T> The controller class.
     * @return The controller instance.
     * @see #create(Class)
     */
    public static <T extends Controller> T createWithNewStage(Class<T> controllerClass) {
        return create(controllerClass, new Stage());
    }


    protected @Getter Stage stage;
    protected @Getter Scene scene;

    /**
     * @return This controller's FXML resource path.
     */
    public String getFXMLResourcePath() {
        Annotation[] annotations = this.getClass().getAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation instanceof FXMLResource) {
                return ((FXMLResource) annotation).value();
            }
        }

        // This should never occur as it's one of the prerequisites to create this class.
        throw new RuntimeException(this.getClass().getName()
                + " doesn't have an FXMLResource annotation");
    }

    /**
     * Shows this controller's stage if it contains one, and is not visible already. Otherwise,
     * does nothing.
     */
    public void show() {
        if (stage != null) {
            stage.show();
        } else {
            LOGGER.log(Level.WARNING, this.getClass().getName()
                    + "#show() was called, but this controller was never provided a stage.");
        }
    }

    /**
     * Hides this controller's stage if it contains one, and is already visible. Otherwise, does
     * nothing.
     */
    public void hide() {
        if (stage != null) {
            stage.hide();
        } else {
            LOGGER.log(Level.WARNING, this.getClass().getName()
                    + "#hide() was called, but this controller was never provided a stage.");
        }
    }

    /**
     * Override this method to customize the provided stage when this method is called. Passes a
     * stage to the controller to set up (in terms of giving the screen, settings titles,
     * etc). This method should not call {@link Stage#show()} or any other visibility calls,
     * otherwise undefined behavior will occur.
     * @param stage The stage to set up.
     */
    public void onStageProvide(Stage stage) {
        this.stage = stage;
        stage.setScene(scene);
    }

    /**
     * Cleans up and destroys internal references to JavaFX objects. Any implementing class
     * should override this method and call this super method after the subclass's cleanup
     * methods are already handled. This method is called when the entire program is about to
     * shut down. This method is NOT CALLED when this controller's stage is closed or hidden.
     */
    public void onShutdown() {
        if (stage != null && stage.isShowing()) {
            stage.close();
        }
    }

    // Optional "abstract" methods that aren't required to be overridden, but can be if the
    // implementor requires them.
    /**
     * Called after the scene has been set for this controller. At this point, any object fields
     * annotated with @{@link javafx.fxml.FXML} are populated with their respective components.
     */
    protected void onSceneCreate() {
        // Method stub.
    }

    /**
     * This method is called from the JavaFX thread, usually on an animation timer. Here a
     * controller may update any items on scene.
     * @param now The timestamp of the current frame.
     */
    public void onUpdate(long now) {
        // Method stub.
    }
}
