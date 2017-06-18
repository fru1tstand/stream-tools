package me.fru1t.streamtools.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import me.fru1t.javafx.Controller;
import me.fru1t.javafx.FXMLResource;


@FXMLResource("/FXML/MainMenu.fxml")
public class MainMenuController extends Controller {
    // The elements within the windowListView
    private static class WindowListItem {
        final Stage stage;
        final Controller controller;

        public WindowListItem(Stage stage, Controller controller) {
            this.stage = stage;
            this.controller = controller;
        }
    }

    private static final String DEFAULT_WINDOW_TITLE = "<no title>";
    private static final String TITLE = "Fru1tstand's Stream Tools";

    // Element declarations
    private @FXML ListView<WindowListItem> windowListView;

    // Class variables
    private final ObservableList<WindowListItem> windowList;

    public MainMenuController() {
        // Wire list view to show what's in the windowList
        windowList = FXCollections.observableArrayList();
        Platform.runLater(() -> {
            windowListView.setItems(windowList);
            windowListView.setCellFactory(param -> new ListCell<WindowListItem>() {
                @Override
                protected void updateItem(WindowListItem item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText("");
                    } else if (item == null) {
                        setText(DEFAULT_WINDOW_TITLE);
                    } else {
                        setText(item.controller.toString());
                    }
                }
            });
        });
    }

    @Override
    public void setUpStage(Stage stage) {
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle(TITLE);
        stage.setOnCloseRequest(event -> Platform.exit());
    }

    @FXML
    private void onAddWindowButtonAction() {
        Stage stage = new Stage();
        Controller controller = Controller.create(TextStatsController.class);
        controller.setUpStage(stage);
        windowList.add(new WindowListItem(stage, controller));
        stage.show();
    }
}
