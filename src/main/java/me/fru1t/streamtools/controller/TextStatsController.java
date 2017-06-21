package me.fru1t.streamtools.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import me.fru1t.javafx.FXMLResource;
import javafx.fxml.FXML;
import me.fru1t.javafx.FXUtils;
import me.fru1t.streamtools.StatisticsCore;
import me.fru1t.streamtools.controller.settings.TextStatsSettings;
import me.fru1t.streamtools.javafx.WindowWithSettingsController;


@FXMLResource("/FXML/TextStats.fxml")
public class TextStatsController
        extends WindowWithSettingsController<TextStatsSettings, TextStatsSettingsController>
        implements StatisticsCore.Events {
    private static final String TEXT_STATS_LABEL_STYLE = "-fx-font-family: \"%s\"; "
            + "-fx-font-size: %dpx; "
            + "-fx-text-alignment: %s; "
            + "-fx-font-weight: %s; "
            + "-fx-font-style: %s; "
            + "-fx-color: %s;";
    private static final String ROOT_STYLE = "-fx-background-color: %s;";

    private @FXML Label textStatsLabel;

    @Override
    public void onStatsUpdate(int keyboardAPM, long mouseMPM) {
        textStatsLabel.setText("APM: " + keyboardAPM + "\nMPM: " + mouseMPM);
    }

    @Override
    public void onSettingsChange(TextStatsSettings settings) {
        textStatsLabel.setStyle(String.format(TEXT_STATS_LABEL_STYLE,
                settings.getFont(),
                settings.getSize(),
                settings.getAlign(),
                (settings.isBold() ? "bold" : "normal"),
                (settings.isItalic() ? "italic" : "normal"),
                FXUtils.colorToHex(settings.getColor())));
        textStatsLabel.setPrefWidth(settings.getWindowWidth() - 20);
        scene.getRoot().setStyle(String.format(ROOT_STYLE,
                FXUtils.colorToHex(settings.getBackgroundColor())));
        stage.setWidth(settings.getWindowWidth());
        stage.setHeight(settings.getWindowHeight());
    }
}
