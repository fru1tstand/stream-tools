package me.fru1t.streamtools.javafx

import javafx.beans.value.ObservableValue
import me.fru1t.javafx.Controller
import me.fru1t.streamtools.controller.Settings

import java.lang.reflect.ParameterizedType

/**
 * A [Controller] that controls itself (eg. a graph or text or some other visual thing), and a
 * secondary [settingsController] that handles this [Controller]'s settings. Requires the [Settings]
 * class [S] and the [SettingsController] class [T].
 */
abstract class WindowWithSettingsController<S : Settings<S>, T : SettingsController<S>> :
    Controller() {

  /** The [SettingsController] that will handle this window's [Settings]. */
  val settingsController: SettingsController<S>

  init {
    // Grab the class WindowWithSettingsController<T>
    val windowWithSettingsControllerClass = javaClass.genericSuperclass as ParameterizedType

    // Get <T>'s class by casting the Type object from the generic <T> parameter
    @Suppress("UNCHECKED_CAST")
    val settingsControllerClass =
        windowWithSettingsControllerClass.actualTypeArguments[1] as Class<T>

    settingsController = Controller.createWithNewStage(settingsControllerClass)
    settingsController.addOnSettingsChangeListener(::onSettingsChange)
  }

  override fun shutdown() {
    settingsController.shutdown()
    super.shutdown()
  }

  override fun show() {
    if (stage != null) {
      stage!!.widthProperty().removeListener(this::onSizeChange)
      stage!!.heightProperty().removeListener(this::onSizeChange)
      stage!!.widthProperty().addListener(this::onSizeChange)
      stage!!.heightProperty().addListener(this::onSizeChange)
    }

    onSettingsChange(settingsController.currentSettings)
    super.show()
  }

  /** Used to update layout when the stage size changes. */
  private fun onSizeChange(obs: ObservableValue<out Number>?, old: Number?, new: Number?) {
    onSettingsChange(settingsController.currentSettings)
  }

  /** Updates the GUI from the [settings]. */
  protected abstract fun onSettingsChange(settings: S)
}
