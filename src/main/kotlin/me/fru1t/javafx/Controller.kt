package me.fru1t.javafx

import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage

import java.io.IOException
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Handles creation and destroying of FXML resource stages and scenes. Each FXML file should have
 * a corresponding Controller class that handles the resource file's events and interactions.
 * Within the FXML file, the root element should have the <code>fx:controller</code> attribute
 * defined like so:
 * ```
 * <RootElement attribute1="value1"
 *              fx:controller="fully.qualified.name.to.my.controller">
 *   ...
 * </RootElement>
 * ```
 *
 * The controller class MUST be annotated with the {@link FXMLResource} annotation in order
 * for this class to work properly:
 * ```
 *   @FXMLResource("path_to.fxml")
 *   public class MyClass extends Controller { ... }
 * ```
 *
 * A controller's lifecycle is as follows:
 *   1. {@link #create(Class)} or any derivative #create method is called which
 *     triggers the FXMLLoader to instantiate a Controller object.
 *   2. {@link #onSceneCreate()} is immediately called. Here, all
 *     {@link javafx.fxml.FXML} annotated fields within the controller are already populated.
 *   3. {@link #onStageProvide(Stage)} is eventually called. Note that this call
 *     may not immediately proceed the #onSceneCreate call as the implementor may wait to give
 *     a stage to the controller. A call to {@link #create(Class, Stage)} will
 *     guarantee that #onStageProvide follows immediately after #onSceneCreate.
 *   4. At this point, the controller is done setting up and normal usage may be assumed.
 *   5. {@link #onShutdown()} is eventually called.
 */
abstract class Controller {
  var stage: Stage? = null
    private set
  lateinit var scene: Scene
    private set

  /** @return This controller's FXML resource path. */
  fun getFxmlResourcePath() = this::class.java.superclass
      .getDeclaredAnnotation(FXMLResource::class.java)!!
      .value

  /**
   * Shows this controller's stage if it contains one, and is not visible already. Otherwise,
   * does nothing.
   */
  open fun show() {
    if (stage != null) {
      stage!!.show()
    } else {
      LOGGER.log(
          Level.WARNING,
          this.javaClass.name
              + "#show() was called, but this controller was never provided a stage.")
    }
  }

  /**
   * Hides this controller's stage if it contains one, and is already visible. Otherwise, does
   * nothing.
   */
  open fun hide() {
    if (stage != null) {
      stage!!.hide()
    } else {
      LOGGER.log(
          Level.WARNING,
          this.javaClass.name
              + "#hide() was called, but this controller was never provided a stage.")
    }
  }

  /**
   * Override this method to customize the provided stage when this method is called. Passes a
   * stage to the controller to set up (in terms of giving the screen, settings titles,
   * etc). This method should not call [Stage.show] or any other visibility calls,
   * otherwise undefined behavior will occur.
   * @param stage The stage to set up.
   */
  // TODO: rename/create provideStage function
  open fun onStageProvide(stage: Stage) {
    this.stage = stage
    stage.scene = scene
  }

  /**
   * Cleans up and destroys internal references to JavaFX objects. Any implementing class
   * should override this method and *call this super method after* the subclass's cleanup
   * methods are already handled. This method is called when the entire program is about to
   * shut down. This method is NOT CALLED when this controller's stage is closed or hidden.
   */
  open fun onShutdown() {
    if (stage != null && stage!!.isShowing) {
      stage!!.close()
    }
  }

  // Optional "abstract" methods that aren't required to be overridden, but can be if the
  // implementor requires them.
  /**
   * Called after the scene has been set for this controller. At this point, any object fields
   * annotated with @[javafx.fxml.FXML] are populated with their respective components.
   */
  protected open fun onSceneCreate() {
    // Method stub.
  }

  /**
   * This method is called from the JavaFX thread, usually on an animation timer. Here a
   * controller may update any items on scene.
   * @param now The timestamp of the current frame.
   */
  open fun onUpdate(now: Long) {
    // Method stub.
  }

  companion object {
    private val LOGGER = Logger.getLogger(Controller::class.java.name)

    /**
     * Creates a new instance of an FXML layout returning the controller that controls it.
     * @param controllerClass The controller class for the FXML layout.
     * @param <T> The controller class for the FXML layout.
     * @return The controller to the new layout instance.
     */
    fun <T : Controller> create(controllerClass: Class<T>): T {
      // Get spec annotation
      val fxmlResourceAnnotation =
          controllerClass.getDeclaredAnnotation(FXMLResource::class.java)
              ?: throw RuntimeException(
                  controllerClass.toString()
                      + " requires the "
                      + FXMLResource::class.java.toString()
                      + " annotation.")

      // Get resource path
      val resourceUrl =
          controllerClass.getResource(fxmlResourceAnnotation.value)
              ?: throw RuntimeException(
                  "Couldn't find the FXML file '"
                      + fxmlResourceAnnotation.value
                      + "' from controller "
                      + controllerClass.toString())

      // Load the fxml
      val loader = FXMLLoader(resourceUrl)
      val fxmlRoot: Parent = loader.load()

      // Pass the root parent to the controller through a scene.
      val controller =
          loader.getController<T>()
              ?: throw RuntimeException(
                  "FXML file "
                      + fxmlResourceAnnotation.value
                      + " has no "
                      + "fx:controller defined in the root element.")
      controller.scene = Scene(fxmlRoot)
      controller.onSceneCreate()

      return controller
    }

    /**
     * Creates and provides a new instance of an FXML layout.
     * @param controllerClass The controller to create.
     * @param stage The stage to hand the controller.
     * @param <T> The type of controller.
     * @return The controller object.
     * @see .create
    </T> */
    fun <T : Controller> create(controllerClass: Class<T>, stage: Stage): T {
      val controller = create(controllerClass)
      controller.onStageProvide(stage)
      return controller
    }

    /**
     * Creates a new instance of an FXML layout passing it a new stage.
     * @param controllerClass The controller class to create.
     * @param <T> The controller class.
     * @return The controller instance.
     * @see .create
    </T> */
    fun <T : Controller> createWithNewStage(controllerClass: Class<T>): T {
      return create(controllerClass, Stage())
    }
  }
}
