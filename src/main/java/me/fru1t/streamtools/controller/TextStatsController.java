package me.fru1t.streamtools.controller;

import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import me.fru1t.javafx.FxmlResource;
import me.fru1t.streamtools.controller.settings.TextStatsSettings;
import me.fru1t.streamtools.javafx.WindowWithSettingsController;
import me.fru1t.streamtools.util.KeyboardAndMouseStatistics;

/**
 * Controls the TextStats window.
 */
@FxmlResource("/FXML/TextStats.fxml")
public class TextStatsController
        extends WindowWithSettingsController<TextStatsSettings, TextStatsSettingsController> {

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
    private final KeyboardAndMouseStatistics stats;

    /**
     * Use {@link me.fru1t.javafx.Controller.Companion#create(Class)} instead.
     */
    public TextStatsController() {
        stats = new KeyboardAndMouseStatistics();
    }

    @Override
    public void onUpdate(long now) {
        stats.tick();
        KeyboardAndMouseStatistics.CurrentData data = stats.getCurrentData();
        textStatsLabel.setText(content
                .replace(ACTIONS_PER_MINUTE_PLACEHOLDER, data.getKeyboardAPM() + "")
                .replace(PIXELS_PER_MINUTE_PLACEHOLDER, data.getMousePPM() + "")
                .replace(TOTAL_ACTIONS_PLACEHOLDER, data.getTotalActions() + "")
                .replace(TOTAL_PIXELS_PLACEHOLDER, data.getTotalPixels() + ""));
    }

    @Override
    protected void onSceneCreate() {
        super.onSceneCreate();
        GridPane.setFillWidth(textStatsLabel, true);
    }

    @Override
    public void shutdown() {
        stats.shutdown();
        super.shutdown();
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

        if (settings.getAlign().equals(TextStatsSettings.Companion.getALIGN_CENTER())) {
            GridPane.setHalignment(textStatsLabel, HPos.CENTER);
        } else if (settings.getAlign().equals(TextStatsSettings.Companion.getALIGN_LEFT())) {
            GridPane.setHalignment(textStatsLabel, HPos.LEFT);
        } else {
            GridPane.setHalignment(textStatsLabel, HPos.RIGHT);
        }

        getScene().getRoot().setStyle(String.format(ROOT_STYLE, settings.getBackgroundColor()));
        content = settings.getContent();
        stats.setBufferSize(settings.getStatsBufferSize());
    }
}
