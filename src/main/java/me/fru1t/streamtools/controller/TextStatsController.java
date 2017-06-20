package me.fru1t.streamtools.controller;

import javafx.scene.control.Label;
import me.fru1t.javafx.FXMLResource;
import javafx.fxml.FXML;
import me.fru1t.streamtools.StatisticsCore;
import me.fru1t.streamtools.controller.settings.TextStatsSettings;
import me.fru1t.streamtools.javafx.WindowWithSettingsController;


@FXMLResource("/FXML/TextStats.fxml")
public class TextStatsController
        extends WindowWithSettingsController<TextStatsSettings, TextStatsSettingsController>
        implements StatisticsCore.Events {
    private @FXML Label textStatsLabel;


    @Override
    public void onSceneCreate() {
        scene.getRoot().setStyle("-fx-background-color: #0F0");
        textStatsLabel.setStyle("-fx-font-size: 40px; -fx-font-weight: bold");
    }

    @Override
    public void onUpdate(int keyboardAPM, long mouseMPM) {
        textStatsLabel.setText("APM: " + keyboardAPM + "\nMPM: " + mouseMPM);
    }

    @Override
    public void onChange(TextStatsSettings settings) {

    }
}
