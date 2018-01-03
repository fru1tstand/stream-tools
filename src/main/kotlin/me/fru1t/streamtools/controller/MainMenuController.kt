package me.fru1t.streamtools.controller

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import javafx.animation.AnimationTimer
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.fxml.FXML
import javafx.scene.control.Alert
import javafx.scene.control.Button
import javafx.scene.control.ButtonType
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.stage.Stage
import me.fru1t.javafx.Controller
import me.fru1t.javafx.FxmlResource
import me.fru1t.javafx.TextInputDialogUtils
import me.fru1t.streamtools.controller.mainmenu.Window
import me.fru1t.streamtools.javafx.WindowWithSettingsController
import org.apache.commons.lang3.StringUtils
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.ArrayList
import java.util.logging.Level
import java.util.logging.Logger

/**
 * First window that appears that handles window management.
 */
@FxmlResource("/FXML/MainMenu.fxml")
class MainMenuController : Controller() {
  @FXML private lateinit var windowListView: ListView<WindowWithSettingsController<*, *>>
  @FXML private lateinit var renameWindowButton: Button
  @FXML private lateinit var deleteWindowButton: Button
  @FXML private lateinit var showWindowButton: Button
  @FXML private lateinit var hideWindowButton: Button
  @FXML private lateinit var settingsWindowButton: Button

  private val windowList: ObservableList<WindowWithSettingsController<*, *>> =
      FXCollections.observableArrayList()
  private var didSelectItem: Boolean = false
  private var currentlySelectedController: WindowWithSettingsController<*, *>? = null
  private var failedToSaveCount = 0

  init {
    // Creates an animation timer that calls all controllers to refresh.
    val updater = object : AnimationTimer() {
      override fun handle(now: Long) {
        for (controller in windowList) {
          controller.onUpdate(now)
        }
      }
    }
    updater.start()
  }

  override fun onSceneCreate() {
    // Give underlying data set
    windowListView.items = windowList

    // Tell the list how to show the contents of the windowList
    windowListView.setCellFactory { _ ->
      object : ListCell<WindowWithSettingsController<*, *>>() {
        override fun updateItem(item: WindowWithSettingsController<*, *>?, empty: Boolean) {
          super.updateItem(item, empty)
          if (empty || item == null) {
            text = ""
            return
          }

          text = if (StringUtils.isBlank(item.stage!!.title)) BLANK_TITLE_REPLACEMENT
          else item.stage!!.title
          style = if (item.stage!!.isShowing) STYLE_ACTIVE_WINDOW
          else STYLE_HIDDEN_WINDOW

          updateButtonVisibility()
        }
      }
    }

    // onSelectItem()
    windowListView.selectionModel.selectedItemProperty().addListener { _, _, newValue ->
      didSelectItem = true
      currentlySelectedController = newValue
      updateButtonVisibility()
    }

    // Load settings
    loadSettings()
  }

  override fun show() {
    stage!!.isResizable = false
    stage!!.setOnCloseRequest { _ ->
      saveSettings()
      for (controller in windowList) {
        controller.shutdown()
      }
      Platform.exit()
    }
    stage!!.title = MAIN_MENU_TITLE
    super.show()
  }

  @FXML private fun addTextStatsWindowButtonAction() {
    addWindow(TextStatsController::class.java)
  }

  @FXML private fun addGraphStatsWindowButtonAction() {
    addWindow(GraphStatsController::class.java)
  }

  @FXML private fun onWindowListViewClick() {
    deselect()
  }

  @FXML private fun onToolbarClick() {
    deselect()
  }

  @FXML private fun onRenameWindowButtonAction() {
    if (currentlySelectedController == null) {
      LOGGER.log(Level.WARNING, "Attempted to rename a window when there was none " +
          "selected. Did our UI update correctly?")
      return
    }

    val windowTitle =
        TextInputDialogUtils.createShowAndWait(
            RENAME_DIALOG_TITLE, null, RENAME_DIALOG_CONTENT_TEXT) ?: return

    currentlySelectedController!!.stage!!.title = windowTitle
    windowListView.refresh()
    saveSettings()
  }

  @FXML private fun onDeleteWindowButtonAction() {
    if (currentlySelectedController == null) {
      LOGGER.log(Level.WARNING, "Attempted to delete a window when there was none selected. " +
          "Did our UI update correctly?")
      return
    }

    currentlySelectedController!!.shutdown()
    windowList.remove(currentlySelectedController)
    saveSettings()
  }

  @FXML private fun onShowWindowButtonAction() {
    if (currentlySelectedController == null) {
      LOGGER.log(Level.WARNING, "Attempted to show a window when there was none selected. " +
          "Did our UI update correctly?")
      return
    }

    currentlySelectedController!!.show()
    updateButtonVisibility()
    windowListView.refresh()
    saveSettings()
  }

  @FXML private fun onHideWindowButtonAction() {
    if (currentlySelectedController == null) {
      LOGGER.log(Level.WARNING, "Attempted to hide a window when there was none selected. " +
          "Did our UI update correctly?")
      return
    }

    currentlySelectedController!!.hide()
    windowListView.refresh()
    updateButtonVisibility()
    saveSettings()
  }

  @FXML private fun onSettingsWindowButtonAction() {
    if (currentlySelectedController == null) {
      return
    }

    currentlySelectedController!!.settingsController.show()
  }

  private fun deselect() {
    // Deselect items if the user clicked on none of the items.
    if (!didSelectItem) {
      windowListView.selectionModel.clearSelection()
    }
    didSelectItem = false
  }

  private fun updateButtonVisibility() {
    if (currentlySelectedController == null) {
      renameWindowButton.isDisable = true
      deleteWindowButton.isDisable = true
      showWindowButton.isDisable = true
      hideWindowButton.isDisable = true
      settingsWindowButton.isDisable = true
      return
    }

    renameWindowButton.isDisable = false
    deleteWindowButton.isDisable = false
    settingsWindowButton.isDisable = false

    val stage = currentlySelectedController!!.stage
    if (stage != null && stage.isShowing) {
      showWindowButton.isDisable = true
      hideWindowButton.isDisable = false
    } else {
      showWindowButton.isDisable = false
      hideWindowButton.isDisable = true
    }
  }

  private fun <T : WindowWithSettingsController<*, *>> addWindow(windowWithSettingsClass: Class<T>) {
    // Ask for the window name
    val windowTitle =
        TextInputDialogUtils.createShowAndWait(
            ASK_FOR_NAME_DIALOG_TITLE, null, ASK_FOR_NAME_DIALOG_CONTENT_TEXT) ?: return

    // Create the stage and setup events
    val stage = Stage()
    stage.setOnCloseRequest { _ -> windowListView.refresh() }

    // Set up controller
    val controller = Controller.create(windowWithSettingsClass, stage)
    controller.stage!!.title = windowTitle
    controller.show()
    controller.settingsController.show()
    windowList.add(controller)

    saveSettings()
  }

  private fun saveSettings() {
    val result = ArrayList<Window>()
    for (controller in windowList) {
      val window = Window()
      window.controllerClass = controller.javaClass.name
      window.settingsJson = GSON.toJson(controller.settingsController.currentSettings)

      window.title = controller.stage!!.title
      window.stageHeight = controller.stage!!.height
      window.stageWidth = controller.stage!!.width
      window.stageX = controller.stage!!.x
      window.stageY = controller.stage!!.y
      window.isVisible = controller.stage!!.isShowing

      result.add(window)
    }

    try {
      Files.write(Paths.get(SETTINGS_SAVE_FILE), GSON.toJson(result).toByteArray())
    } catch (e: IOException) {
      LOGGER.log(Level.SEVERE, "Couldn't save settings.", e)
      if (failedToSaveCount < MAX_FAILED_TO_SAVE_DIALOGS) {
        failedToSaveCount++
        Alert(
            Alert.AlertType.ERROR,
            String.format(
                FAILED_TO_SAVE_TEXT, e.message, failedToSaveCount, MAX_FAILED_TO_SAVE_DIALOGS),
            ButtonType.OK)
            .showAndWait()
      }
    }

  }

  private fun loadSettings() {
    try {
      val contents = String(Files.readAllBytes(Paths.get(SETTINGS_SAVE_FILE)))
      val windows = GSON.fromJson(contents, Array<Window>::class.java)
      for ((controllerClass, settingsJson, stageWidth, stageHeight, stageX, stageY, title,
          isVisible) in windows) {
        @Suppress("UNCHECKED_CAST")
        val controller =
            Controller.createWithNewStage(
                Class.forName(controllerClass) as Class<WindowWithSettingsController<*, *>>)

        val settings =
            GSON.fromJson<Settings<*>>(
                settingsJson, controller.settingsController.currentSettings.javaClass)

        controller.settingsController.update(settings)
        controller.stage!!.title = title
        controller.stage!!.width = stageWidth
        controller.stage!!.height = stageHeight
        controller.stage!!.x = stageX
        controller.stage!!.y = stageY
        controller.stage!!.setOnCloseRequest { _ -> windowListView.refresh() }

        if (isVisible) {
          controller.show()
        }
        windowList.add(controller)
      }
    } catch (e: JsonSyntaxException) {
      tryRenameSettingsFile(e)
    } catch (e: NumberFormatException) {
      tryRenameSettingsFile(e)
    } catch (e: IOException) {
      LOGGER.log(Level.WARNING, "No settings file was found!")
    } catch (e: ClassNotFoundException) {
      LOGGER.log(Level.WARNING, "Couldn't find controller class ${e.message}")
    }
  }

  companion object {
    private val SETTINGS_SAVE_FILE = "settings.json"

    private val ASK_FOR_NAME_DIALOG_TITLE = "Add Window"
    private val ASK_FOR_NAME_DIALOG_CONTENT_TEXT = "What should the window title be?"
    private val RENAME_DIALOG_TITLE = "Rename Window"
    private val RENAME_DIALOG_CONTENT_TEXT = "What should the new name for the window be?"
    private val MAIN_MENU_TITLE = "Fru1tstand's Stream Tools"
    private val BLANK_TITLE_REPLACEMENT = "<blank title>"

    private val STYLE_ACTIVE_WINDOW = "-fx-text-fill: #000"
    private val STYLE_HIDDEN_WINDOW = "-fx-text-fill: #777; -fx-font-style: italic"

    private val FAILED_TO_SAVE_TEXT = "I was unable to save your settings. This was the error I " +
        "got: %s. This is warning %d. I will stop showing this error after %d times."
    private val MAX_FAILED_TO_SAVE_DIALOGS = 3

    private val LOGGER = Logger.getLogger(MainMenuController::class.java.name)
    private val GSON = Gson()

    /** Attempts to rename the settings file or crashes if it can't. */
    private fun tryRenameSettingsFile(e: Exception) {
      LOGGER.log(Level.WARNING, "Couldn't process the settings file.", e)
      val existingFile = File(SETTINGS_SAVE_FILE)
      var i = 0
      var renamedFile = File(i.toString() + "-" + SETTINGS_SAVE_FILE)
      if (existingFile.exists()) {
        while (renamedFile.exists()) {
          renamedFile = File((++i).toString() + "-" + SETTINGS_SAVE_FILE)
        }

        if (!existingFile.renameTo(renamedFile)) {
          Alert(
              Alert.AlertType.ERROR,
              "Sorry, I was unable to process your settings file. It gave me this error: " +
                  "${ e.message}\n\n I also couldn't rename it, so I will shut down in order " +
                  "to protect your precious settings. To fix, please rename, move, or delete the " +
                  "settings file at ${existingFile.absolutePath}")
              .showAndWait()
          throw RuntimeException("Settings file was invalid and couldn't rename. Shutting down.")
        } else {
          Alert(
              Alert.AlertType.WARNING,
              "Sorry, I was unable to process your settings file. It gave me this error: " +
                  "${e.message}\n\nI have renamed it to ${renamedFile.absolutePath} in order " +
                  "to protect it, and I will continue starting up as if there were no settings " +
                  "file")
              .showAndWait()
        }
      }
    }
  }
}
