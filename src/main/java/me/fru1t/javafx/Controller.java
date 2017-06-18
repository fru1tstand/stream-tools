package me.fru1t.javafx;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;

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
    private static final String DEFAULT_TITLE = "";

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
            throw new RuntimeException(controllerClass.toString() + " requires the" +
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

        return controller;
    }

    protected Scene scene;

    /**
     * Passes a stage to the controller to set up (in terms of giving the screen, setting titles,
     * etc). This method should not call {@link Stage#show()} or any other visibility calls,
     * otherwise undefined behavior will occur.
     * @param stage The stage to set up.
     */
    public abstract void setUpStage(Stage stage);

    @Override
    public String toString() {
        return DEFAULT_TITLE;
    }
}
