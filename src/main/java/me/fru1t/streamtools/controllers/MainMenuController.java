package me.fru1t.streamtools.controllers;

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
    private @FXML ListView<Controller> windowListView;
    private @FXML Button renameWindowButton;
    private @FXML Button deleteWindowButton;
    private @FXML Button showWindowButton;
    private @FXML Button hideWindowButton;

    // Class variables
    private final ObservableList<Controller> windowList;
    private boolean didSelectItem;
    private @Nullable Controller currentlySelectedController;

    public MainMenuController() {
        setTitle(MAIN_MENU_TITLE);

        // Wire list view to show what's in the windowList
        windowList = FXCollections.observableArrayList();
        didSelectItem = false;

        // Set up window list view events and data
        Platform.runLater(() -> {
            // Give underlying data set
            windowListView.setItems(windowList);

            // Tell the list how to show the contents of the windowList
            windowListView.setCellFactory(param -> new ListCell<Controller>() {
                @Override
                protected void updateItem(Controller item, boolean empty) {
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
    }

    @Override
    public void provideStage(Stage stage) {
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle(getTitle());
        stage.setOnCloseRequest(event -> Platform.exit());
    }

    @FXML
    private void addTextStatsWindowButtonAction() {
        // Ask for the window name
        String windowTitle = TextInputDialogUtils.createShowAndWait(ASK_FOR_NAME_DIALOG_TITLE,
                null, ASK_FOR_NAME_DIALOG_CONTENT_TEXT);
        if (windowTitle == null) {
            return;
        }

        // Create the stage and setup events
        Stage stage = new Stage();
        stage.setOnCloseRequest(event -> windowListView.refresh());

        // Set up controller
        Controller controller = Controller.create(TextStatsController.class);
        controller.provideStage(stage);
        controller.setTitle(windowTitle);
        windowList.add(controller);
        stage.show();
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
            return;
        }

        renameWindowButton.setDisable(false);
        deleteWindowButton.setDisable(false);

        Stage stage = currentlySelectedController.getStage();
        if (stage != null && stage.isShowing()) {
            showWindowButton.setDisable(true);
            hideWindowButton.setDisable(false);
        } else {
            showWindowButton.setDisable(false);
            hideWindowButton.setDisable(true);
        }
    }
}
