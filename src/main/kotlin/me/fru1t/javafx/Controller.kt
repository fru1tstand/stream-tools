package me.fru1t.javafx

import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage

import java.util.logging.Level
import java.util.logging.Logger
import javax.annotation.OverridingMethodsMustInvokeSuper

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
 *   5. {@link #shutdown()} is eventually called.
 */
abstract class Controller {
  var stage: Stage? = null
    protected set
  lateinit var scene: Scene
    private set

  /** @return This controller's FXML resource path. */
  fun getFxmlResourcePath() = this.javaClass
      .getDeclaredAnnotation(FxmlResource::class.java)!!
      .value

  /**
   * Shows this controller's stage if it contains one, and is not visible already. Otherwise,
   * does nothing. Overriding methods may modify the stage.
   */
  @OverridingMethodsMustInvokeSuper
  open fun show() {
    if (stage != null) {
      stage!!.scene = scene
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
   * nothing. Overriding methods may modify the stage.
   */
  @OverridingMethodsMustInvokeSuper
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
   * Cleans up and destroys internal references to JavaFX objects. Any implementing class
   * should override this method and *call this super method after* the subclass's cleanup
   * methods are already handled. This method is called when the entire program is about to
   * shut down. This method is NOT CALLED when this controller's stage is closed or hidden.
   */
  @OverridingMethodsMustInvokeSuper
  open fun shutdown() {
    stage?.close()
  }

  // Optional methods that can be overridden.
  /**
   * Called after the scene has been set for this controller. All Fxml fields, scene, and stage are
   * initialized and available to use from this method.
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
     * Inflates the layout associated to [controllerClass] optionally providing a [stage]. Returns
     * the controller [T] with a fully inflated scene.
     */
    fun <T : Controller> create(controllerClass: Class<T>, stage: Stage? = null): T {
      // FxmlResource annotation holds the location of the Fxml resource file
      val fxmlResourceAnnotation =
          controllerClass.getDeclaredAnnotation(FxmlResource::class.java)
              ?: throw RuntimeException(
              controllerClass.toString()
                  + " requires the "
                  + FxmlResource::class.java.toString()
                  + " annotation.")

      // Extract path
      val resourceUrl =
          controllerClass.getResource(fxmlResourceAnnotation.value)
              ?: throw RuntimeException(
              "Couldn't find the FXML file '"
                  + fxmlResourceAnnotation.value
                  + "' from controller "
                  + controllerClass.toString())

      // Inflate the Fxml
      val loader = FXMLLoader(resourceUrl)
      val fxmlRoot: Parent = loader.load()

      // Grab the controller
      val controller =
          loader.getController<T>()
              ?: throw RuntimeException(
              "FXML file "
                  + fxmlResourceAnnotation.value
                  + " has no fx:controller defined in the root element.")

      // Set up fields within the controller
      controller.stage = stage
      controller.scene = Scene(fxmlRoot)
      controller.onSceneCreate()

      return controller
    }

    /**
     * Inflates the layout associated to [controllerClass] creating a new [Stage] and returning
     * the controller [T] with a fully inflated scene.
     */
    fun <T : Controller> createWithNewStage(controllerClass: Class<T>): T {
      return create(controllerClass, Stage())
    }
  }
}
