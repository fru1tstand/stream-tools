package me.fru1t.streamtools.controllers;

import javafx.scene.control.Label;
import me.fru1t.javafx.Controller;
import me.fru1t.javafx.FXMLResource;
import javafx.fxml.FXML;
import me.fru1t.streamtools.StatisticsCore;


@FXMLResource("/FXML/TextStats.fxml")
public class TextStatsController extends Controller implements StatisticsCore.Events {
    private @FXML Label textStatsLabel;


    @Override
    public void onSceneCreate() {
        scene.getRoot().setStyle("-fx-background-color: #0F0");
    }

    @Override
    public void onUpdate(int keyboardAPM, long mouseMPM) {
        textStatsLabel.setText("APM: " + keyboardAPM + "\nMPM: " + mouseMPM);
    }
}
