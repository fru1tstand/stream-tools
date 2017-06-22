package me.fru1t.streamtools.controller;

import javafx.geometry.HPos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import me.fru1t.javafx.FXMLResource;
import javafx.fxml.FXML;
import me.fru1t.javafx.FXUtils;
import me.fru1t.streamtools.StatisticsCore;
import me.fru1t.streamtools.controller.settings.TextStatsSettings;
import me.fru1t.streamtools.javafx.WindowWithSettingsController;

/**
 * Controls the TextStats window.
 */
@FXMLResource("/FXML/TextStats.fxml")
public class TextStatsController
        extends WindowWithSettingsController<TextStatsSettings, TextStatsSettingsController>
        implements StatisticsCore.Events {

    private static final String ACTIONS_PER_MINUTE_PLACEHOLDER = "{apm}";
    private static final String PIXELS_PER_MINUTE_PLACEHOLDER = "{ppm}";
    private static final String TOTAL_ACTIONS_PLACEHOLDER = "{ta}";
    private static final String TOTAL_PIXELS_PLACEHOLDER = "{tp}";

    private static final String TEXT_STATS_LABEL_STYLE = "-fx-font-family: \"%s\"; "
            + "-fx-font-size: %dpx; "
            + "-fx-text-alignment: %s; "
            + "-fx-font-weight: %s; "
            + "-fx-font-style: %s; "
            + "-fx-color: %s; ";
    private static final String ROOT_STYLE = "-fx-background-color: %s;";

    private @FXML Label textStatsLabel;
    private String content;

    @Override
    public void onStatsUpdate(int keyboardAPM, long mousePPM, long totalActions, long totalPixels) {
        textStatsLabel.setText(content
                .replace(ACTIONS_PER_MINUTE_PLACEHOLDER, keyboardAPM + "")
                .replace(PIXELS_PER_MINUTE_PLACEHOLDER, mousePPM + "")
                .replace(TOTAL_ACTIONS_PLACEHOLDER, totalActions + "")
                .replace(TOTAL_PIXELS_PLACEHOLDER, totalPixels + ""));
    }

    @Override
    protected void onSceneCreate() {
        super.onSceneCreate();
        GridPane.setFillWidth(textStatsLabel, true);
    }

    @Override
    public void onSettingsChange(TextStatsSettings settings) {
        textStatsLabel.setStyle(String.format(TEXT_STATS_LABEL_STYLE,
                settings.getFont(),
                settings.getSize(),
                settings.getAlign(),
                (settings.isBold() ? "bold" : "normal"),
                (settings.isItalic() ? "italic" : "normal"),
                settings.getColor()));

        switch (settings.getAlign()) {
            case TextStatsSettings.ALIGN_CENTER:
                GridPane.setHalignment(textStatsLabel, HPos.CENTER);
                break;
            case TextStatsSettings.ALIGN_LEFT:
                GridPane.setHalignment(textStatsLabel, HPos.LEFT);
                break;
            case TextStatsSettings.ALIGN_RIGHT:
                GridPane.setHalignment(textStatsLabel, HPos.RIGHT);
                break;
        }

        scene.getRoot().setStyle(String.format(ROOT_STYLE, settings.getBackgroundColor()));
        content = settings.getContent();
    }
}
