package me.fru1t.streamtools.controller;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import me.fru1t.javafx.Controller;
import me.fru1t.javafx.FXMLResource;
import me.fru1t.javafx.TextInputDialogUtils;
import me.fru1t.streamtools.StatisticsCore;
import me.fru1t.streamtools.javafx.WindowWithSettingsController;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;

/**
 * First window that appears that handles window management.
 */
@FXMLResource("/FXML/MainMenu.fxml")
public class MainMenuController extends Controller {
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
        // Set up main menu window
        setTitle(MAIN_MENU_TITLE);

        // Get stats core
        core = new StatisticsCore();

        // Wire list view to show what's in the windowList
        windowList = FXCollections.observableArrayList();
        didSelectItem = false;

        // Set up window list view events and data
        Platform.runLater(() -> {
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
                        String title = item.getTitle();
                        if (StringUtils.isBlank(title)) {
                            setText(BLANK_TITLE_REPLACEMENT);
                        } else {
                            setText(item.getTitle());
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
        });

        AnimationTimer updater = new AnimationTimer() {
            @Override
            public void handle(long now) {
                core.notifyHandlers();
            }
        };
        updater.start();
    }

    @Override
    public void provideStage(Stage stage) {
        super.provideStage(stage);
        stage.setResizable(false);
        stage.setOnCloseRequest(event -> {
            core.shutdown();
            Platform.exit();
        });
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

        currentlySelectedController.setTitle(windowTitle);
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

        currentlySelectedController.showSettings();
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
        controller.setTitle(windowTitle);
        controller.show();
        controller.showSettings();
        windowList.add(controller);

        return controller;
    }
}
