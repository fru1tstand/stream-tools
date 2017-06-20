package me.fru1t.javafx;

import com.sun.istack.internal.logging.Logger;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.Getter;
import me.fru1t.streamtools.StreamTools;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.logging.Level;

/**
 * Inflates and handles FXML resource file instances. Each FXML file should have a corresponding
 * Controller class that handles the resource file's events and interactions. Within the FXML
 * file, the root element should have the <code>fx:controller</code> attribute defined like so:
 * <pre>
 *     <RootElement attribute1="value1"
 *                  fx:controller="fully.qualified.name.to.my.controller">
 *          ...
 *     </RootElement>
 * </pre>
 * The controller class MUST be annotated with the {@link FXMLResource} annotation in order
 * for this class to work properly.
 */
public abstract class Controller {
    private static final Logger LOGGER = Logger.getLogger(StreamTools.class);

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
            // We errored while opening a valid URL. This is indicative of a larger problem that's
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
        controller.provideStage(stage);
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
    private @Getter String title;

    /**
     * Called after the scene has been set for this controller.
     */
    protected void onSceneCreate() {
        // Method stub.
    }

    /**
     * Sets the title the stage should be set to. Sets the stage title if one exists within the
     * controller.
     * @param title The title.
     */
    public void setTitle(String title) {
        this.title = title;
        if (stage != null) {
            stage.setTitle(title);
        }
    }

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
     * Override this method to customize the provided stage when this method is called. Passes a
     * stage to the controller to set up (in terms of giving the screen, settings titles,
     * etc). This method should not call {@link Stage#show()} or any other visibility calls,
     * otherwise undefined behavior will occur.
     * @param stage The stage to set up.
     */
    public void provideStage(Stage stage) {
        this.stage = stage;
        stage.setTitle(title);
        stage.setScene(scene);
    }

    public void show() {
        if (stage != null) {
            stage.show();
        } else {
            LOGGER.log(Level.WARNING, this.getClass().getName()
                    + "#show() was called, but was never provided a stage.");
        }
    }

    public void hide() {
        if (stage != null) {
            stage.hide();
        } else {
            LOGGER.log(Level.WARNING, this.getClass().getName()
                    + "#hide() was called, but was never provided a stage.");
        }
    }
}
