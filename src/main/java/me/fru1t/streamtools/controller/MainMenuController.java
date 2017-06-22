package me.fru1t.streamtools.controller;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import me.fru1t.javafx.Controller;
import me.fru1t.javafx.FXMLResource;
import me.fru1t.javafx.TextInputDialogUtils;
import me.fru1t.streamtools.Settings;
import me.fru1t.streamtools.StatisticsCore;
import me.fru1t.streamtools.Window;
import me.fru1t.streamtools.javafx.WindowWithSettingsController;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * First window that appears that handles window management.
 */
@FXMLResource("/FXML/MainMenu.fxml")
public class MainMenuController extends Controller {
    private static final String SETTINGS_SAVE_FILE = "settings.json";

    private static final String ASK_FOR_NAME_DIALOG_TITLE = "Add Window";
    private static final String ASK_FOR_NAME_DIALOG_CONTENT_TEXT =
            "What should the window title be?";
    private static final String RENAME_DIALOG_TITLE = "Rename Window";
    private static final String RENAME_DIALOG_CONTENT_TEXT =
            "What should the new name for the window be?";

    private static final String MAIN_MENU_TITLE = "Fru1tstand's Stream Tools";
    private static final String BLANK_TITLE_REPLACEMENT = "<blank title>";

    private static final String STYLE_ACTIVE_WINDOW = "-fx-text-fill: #000";
    private static final String STYLE_HIDDEN_WINDOW = "-fx-text-fill: #777; -fx-font-style: italic";

    private static final Logger LOGGER = Logger.getLogger(MainMenuController.class.getName());
    private static final Gson GSON = new Gson();

    // Element declarations
    private @FXML ListView<WindowWithSettingsController<?, ?>> windowListView;
    private @FXML Button renameWindowButton;
    private @FXML Button deleteWindowButton;
    private @FXML Button showWindowButton;
    private @FXML Button hideWindowButton;
    private @FXML Button settingsWindowButton;

    // Class variables
    private final ObservableList<WindowWithSettingsController<?, ?>> windowList;
    private final StatisticsCore core;
    private boolean didSelectItem;
    private @Nullable WindowWithSettingsController<?, ?> currentlySelectedController;

    public MainMenuController() {
        // Get stats core
        core = new StatisticsCore();

        // Wire list view to show what's in the windowList
        windowList = FXCollections.observableArrayList();
        didSelectItem = false;

        AnimationTimer updater = new AnimationTimer() {
            @Override
            public void handle(long now) {
                core.notifyHandlers();
            }
        };
        updater.start();
    }

    @Override
    protected void onSceneCreate() {
        super.onSceneCreate();

        // Give underlying data set
        windowListView.setItems(windowList);

        // Tell the list how to show the contents of the windowList
        windowListView.setCellFactory(param -> new ListCell<WindowWithSettingsController<?, ?>>() {
            @Override
            protected void updateItem(WindowWithSettingsController<?, ?> item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                } else {
                    String title = item.getStage().getTitle();
                    if (StringUtils.isBlank(title)) {
                        setText(BLANK_TITLE_REPLACEMENT);
                    } else {
                        setText(item.getStage().getTitle());
                    }

                    setStyle(item.getStage().isShowing()
                            ? STYLE_ACTIVE_WINDOW : STYLE_HIDDEN_WINDOW);

                    updateButtonVisibility();
                }
            }
        });

        // onSelectItem()
        windowListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    didSelectItem = true;
                    currentlySelectedController = newValue;
                    updateButtonVisibility();
                });

        // Load settings
        load();
    }

    @Override
    public void provideStage(Stage stage) {
        super.provideStage(stage);
        stage.setResizable(false);
        stage.setOnCloseRequest(event -> {
            core.shutdown();
            save();
            Platform.exit();
        });
        stage.setTitle(MAIN_MENU_TITLE);
    }

    @FXML
    private void addTextStatsWindowButtonAction() {
        TextStatsController controller = addWindow(TextStatsController.class);
        core.addEventHandler(controller);
    }

    @FXML
    private void onWindowListViewClick() {
        deselect();
    }

    @FXML
    private void onToolbarClick() {
        deselect();
    }

    @FXML
    private void onRenameWindowButtonAction() {
        if (currentlySelectedController == null) {
            return;
        }

        String windowTitle = TextInputDialogUtils.createShowAndWait(RENAME_DIALOG_TITLE, null,
                RENAME_DIALOG_CONTENT_TEXT);
        if (windowTitle == null) {
            return;
        }

        currentlySelectedController.getStage().setTitle(windowTitle);
        windowListView.refresh();
    }

    @FXML
    private void onDeleteWindowButtonAction() {
        if (currentlySelectedController == null) {
            return;
        }

        Stage stage = currentlySelectedController.getStage();
        if (stage != null && stage.isShowing()) {
            stage.close();
        }
        if (currentlySelectedController instanceof StatisticsCore.Events) {
            core.removeEventHandler((StatisticsCore.Events) currentlySelectedController);
        }
        windowList.remove(currentlySelectedController);
    }

    @FXML
    private void onShowWindowButtonAction() {
        if (currentlySelectedController == null) {
            return;
        }

        Stage stage = currentlySelectedController.getStage();
        if (stage != null) {
            stage.show();
            updateButtonVisibility();
            windowListView.refresh();
        }
    }

    @FXML
    private void onHideWindowButtonAction() {
        if (currentlySelectedController == null) {
            return;
        }

        Stage stage = currentlySelectedController.getStage();
        if (stage != null) {
            stage.hide();
            windowListView.refresh();
            updateButtonVisibility();
        }
    }

    @FXML
    private void onSettingsWindowButtonAction() {
        if (currentlySelectedController == null) {
            return;
        }

        currentlySelectedController.getSettingsController().show();
    }

    private void deselect() {
        // Deselect items if the user clicked on none of the items.
        if (!didSelectItem) {
            windowListView.getSelectionModel().clearSelection();
        }
        didSelectItem = false;
    }

    private void updateButtonVisibility() {
        if (currentlySelectedController == null) {
            renameWindowButton.setDisable(true);
            deleteWindowButton.setDisable(true);
            showWindowButton.setDisable(true);
            hideWindowButton.setDisable(true);
            settingsWindowButton.setDisable(true);
            return;
        }

        renameWindowButton.setDisable(false);
        deleteWindowButton.setDisable(false);
        settingsWindowButton.setDisable(false);

        Stage stage = currentlySelectedController.getStage();
        if (stage != null && stage.isShowing()) {
            showWindowButton.setDisable(true);
            hideWindowButton.setDisable(false);
        } else {
            showWindowButton.setDisable(false);
            hideWindowButton.setDisable(true);
        }
    }

    private <T extends WindowWithSettingsController<?, ?>> T addWindow(
            Class<T> windowWithSettingsClass) {
        // Ask for the window name
        String windowTitle = TextInputDialogUtils.createShowAndWait(
                ASK_FOR_NAME_DIALOG_TITLE, null, ASK_FOR_NAME_DIALOG_CONTENT_TEXT);
        if (windowTitle == null) {
            return null;
        }

        // Create the stage and setup events
        Stage stage = new Stage();
        stage.setOnCloseRequest(event -> windowListView.refresh());

        // Set up controller
        T controller = Controller.create(windowWithSettingsClass, stage);
        controller.getStage().setTitle(windowTitle);
        controller.show();
        controller.getSettingsController().show();
        windowList.add(controller);

        save();

        return controller;
    }

    private void save() {
        ArrayList<Window> result = new ArrayList<>();
        for (WindowWithSettingsController<?, ?> controller : windowList) {
            Window window = new Window();
            window.controllerClass = controller.getClass().getName();
            window.settingsJson =
                    GSON.toJson(controller.getSettingsController().getCurrentSettings());

            window.title = controller.getStage().getTitle();
            window.stageHeight = controller.getStage().getHeight();
            window.stageWidth = controller.getStage().getWidth();
            window.stageX = controller.getStage().getX();
            window.stageY = controller.getStage().getY();

            result.add(window);
        }

        try {
            Files.write(Paths.get(SETTINGS_SAVE_FILE), GSON.toJson(result).getBytes());
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Couldn't save settings: " + e.getMessage());
        }

    }

    private void load() {
        try {
            String contents = new String(Files.readAllBytes(Paths.get(SETTINGS_SAVE_FILE)));
            Window[] windows = GSON.fromJson(contents, Window[].class);
            for (Window window : windows) {
                @SuppressWarnings("unchecked")
                WindowWithSettingsController<?, ?> controller = Controller.createWithNewStage(
                        (Class<WindowWithSettingsController<?, ?>>)
                                Class.forName(window.controllerClass));

                Settings<?> settings = GSON.fromJson(window.settingsJson,
                        controller.getSettingsController().getCurrentSettings().getClass());

                controller.getSettingsController().update(settings);
                controller.getStage().setTitle(window.title);
                controller.getStage().setWidth(window.stageWidth);
                controller.getStage().setHeight(window.stageHeight);
                controller.getStage().setX(window.stageX);
                controller.getStage().setY(window.stageY);

                controller.show();
                windowList.add(controller);

                // Add to event handler if present.
                if (controller instanceof StatisticsCore.Events) {
                    core.addEventHandler((StatisticsCore.Events) controller);
                }
            }
        } catch (JsonSyntaxException e) {
            LOGGER.log(Level.WARNING, e.getMessage());
            File existingFile = new File(SETTINGS_SAVE_FILE);
            int i = 0;
            File renamedFile = new File(i + "-" + SETTINGS_SAVE_FILE);
            if (existingFile.exists()) {
                while (renamedFile.exists()) {
                    renamedFile = new File(++i + "-" + SETTINGS_SAVE_FILE);
                }

                if (!existingFile.renameTo(renamedFile)) {
                    new Alert(Alert.AlertType.ERROR, "Sorry, I was unable to process your "
                            + "settings file. It gave me this error: " + e.getMessage() + "\n\n I"
                            + " also couldn't rename it, so I will shut down in order to protect "
                            + "your precious settings. To fix, please rename, move, or delete the"
                            + " settings file at " + existingFile.getAbsolutePath())
                            .showAndWait();
                    throw new RuntimeException("Settings file was invalid and couldn't rename. "
                            + "Shutting down.");
                } else {
                    new Alert(Alert.AlertType.WARNING, "Sorry, I was unable to process your "
                            + "settings file. It gave me this error: " + e.getMessage() + "\n\nI "
                            + "have renamed it to " + renamedFile.getAbsolutePath() + " in order "
                            + "to protect it, and I will continue starting up as if there were no"
                            + " settings file")
                            .showAndWait();
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "No settings file was found!");
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.WARNING, "Couldn't find controller class: " + e.getMessage());
        }
    }
}
