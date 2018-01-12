package me.fru1t.streamtools.javafx

import java.lang.reflect.InvocationTargetException
import javafx.fxml.FXML
import javafx.scene.control.Button
import javax.annotation.OverridingMethodsMustInvokeSuper
import me.fru1t.javafx.Controller
import me.fru1t.kotlin.ConcurrentWeakReferenceHashSet
import me.fru1t.streamtools.controller.Settings
import java.lang.ref.WeakReference

import java.lang.reflect.ParameterizedType

/**
 * A controller that manages a [Settings] object specified by [T] which is updated through the GUI.
 * Layouts that extend this base class must include a Cancel, Save, and Apply button.
 */
abstract class SettingsController<T : Settings<T>> protected constructor() : Controller() {

  // FXML Declarations
  @FXML private lateinit var settingsCancelButton: Button
  @FXML private lateinit var settingsSaveButton: Button
  @FXML private lateinit var settingsApplyButton: Button

  // Class declarations
  val currentSettings: T

  private val onSettingsChangeListeners: ConcurrentWeakReferenceHashSet<(T) -> Unit> =
      ConcurrentWeakReferenceHashSet()

  init {
    // We need our own copy of settings, in order to do that, we just create a new instance of
    // T (our settings type).

    // Grab this class (SettingsController<T>) as a parametrized type from the implementing
    // class's definition.
    val settingsControllerClass = javaClass.genericSuperclass as ParameterizedType

    // Get the class type T
    @Suppress("UNCHECKED_CAST")
    val settingsClass = settingsControllerClass.actualTypeArguments[0] as Class<T>

    // Create an instance of it with the no-arg constructor
    try {
      val settingsConstructor = settingsClass.getConstructor()
      currentSettings = settingsConstructor.newInstance()
    } catch (e: NoSuchMethodException ) {
      throw RuntimeException(e)
    } catch (e: IllegalAccessException) {
      throw RuntimeException(e)
    } catch (e: InstantiationException) {
      throw RuntimeException(e)
    } catch (e: InvocationTargetException) {
      throw RuntimeException(e)
    }

    // Add ourselves to the list of event handlers
    addOnSettingsChangeListener(::onSettingsChange)
  }

  @OverridingMethodsMustInvokeSuper
  override fun onSceneCreate() {
    super.onSceneCreate()

    // Kotlin's lateinit allows us to denote fields as non-null, but they may be null. We want to
    // fail early if this happens.
    @Suppress("SENSELESS_COMPARISON")
    if (settingsCancelButton == null) {
      throw RuntimeException(
          "${getFxmlResourcePath()} requires a button with an fx:id of \"settingsCancelButton\"")
    }
    @Suppress("SENSELESS_COMPARISON")
    if (settingsSaveButton == null) {
      throw RuntimeException(
          "${getFxmlResourcePath()} requires a button with an fx:id of \"settingsSaveButton\"")
    }
    @Suppress("SENSELESS_COMPARISON")
    if (settingsApplyButton == null) {
      throw RuntimeException(
          "${getFxmlResourcePath()} requires a button with an fx:id of \"settingsApplyButton\"")
    }

    // Wire cancel and save buttons
    settingsCancelButton.setOnAction { _ -> this@SettingsController.stage!!.hide() }
    settingsApplyButton.setOnAction { _ ->
      commitSettings()
      onSettingsChangeListeners.forEach { it(currentSettings.copy()) }
    }
    settingsSaveButton.setOnAction { _ ->
      commitSettings()
      onSettingsChangeListeners.forEach { it(currentSettings.copy()) }
      this@SettingsController.stage!!.hide()
    }

    // All controls should lose focus when the root is clicked
    scene.root.setOnMouseClicked { _ -> scene.root.requestFocus() }
  }

  @OverridingMethodsMustInvokeSuper
  override fun show() {
    stage?.isResizable = false
    onSettingsChange(currentSettings)
    super.show()
  }

  /**
   * Adds a listener that is fired every time these settings change. The listener is passed a copy
   * of the settings. Listeners are stored as [WeakReference]s, so they will be automatically
   * garbage collected once the underlying object is gone.
   */
  fun addOnSettingsChangeListener(listener: (T) -> Unit) {
    onSettingsChangeListeners.add(listener)
  }

  /** Updates the current settings with the given [settings], making a defensive copy. */
  fun update(settings: Settings<*>) {
    currentSettings.update(settings)
    onSettingsChangeListeners.forEach { it(currentSettings.copy()) }
  }

  /** Commit all GUI changes to the [currentSettings] object. */
  protected abstract fun commitSettings()

  /** Updates the GUI from the [currentSettings] object (or [settings]). */
  protected abstract fun onSettingsChange(settings: T)
}
