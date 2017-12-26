package me.fru1t.streamtools.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.text.Font;
import javafx.util.StringConverter;
import me.fru1t.javafx.FXMLResource;
import me.fru1t.javafx.FXUtils;
import me.fru1t.javafx.FXUtils.FieldType;
import me.fru1t.javafx.FXUtils.SliderTextFieldType;
import me.fru1t.streamtools.controller.settings.GraphStatsSettings;
import me.fru1t.streamtools.controller.settings.GraphStatsSettings.Statistic;
import me.fru1t.streamtools.javafx.SettingsController;

/**
 * Controller for the GraphStatsSettings FXML resource that defines settings for
 * {@link GraphStatsController}
 */
@FXMLResource("/FXML/GraphStatsSettings.fxml")
public class GraphStatsSettingsController extends SettingsController<GraphStatsSettings> {
    private static final ObservableList<Integer> DEFAULT_TEXT_SIZES =
            FXCollections.observableArrayList(
                    1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 14, 16, 18, 22, 26, 32, 64, 72);
    // General
    private @FXML Slider statsSamplesSlider;
    private @FXML TextField statsSamplesField;
    private @FXML Slider historyWindowSlider;
    private @FXML TextField historyWindowField;
    private @FXML Slider pointsSlider;
    private @FXML TextField pointsField;
    private @FXML TextField paddingTop;
    private @FXML TextField paddingLeft;
    private @FXML TextField paddingRight;
    private @FXML TextField paddingBottom;
    private @FXML ColorPicker backgroundColor;
    private @FXML TextField minValue;
    private @FXML TextField maxValue;

    // Visuals
    private @FXML CheckBox dots;
    private @FXML TextField dotSize;
    private @FXML ColorPicker dotColor;
    private @FXML CheckBox line;
    private @FXML TextField lineSize;
    private @FXML ColorPicker lineColor;
    private @FXML CheckBox bars;
    private @FXML TextField barSize;
    private @FXML ColorPicker barColor;
    private @FXML ChoiceBox<Statistic> statistic;

    // Time Axis
    private @FXML CheckBox timeAxis;
    private @FXML TextField timeAxisMajorEvery;
    private @FXML TextField timeAxisMajorWidth;
    private @FXML ColorPicker timeAxisMajorColor;
    private @FXML TextField timeAxisMinorEvery;
    private @FXML TextField timeAxisMinorWidth;
    private @FXML ColorPicker timeAxisMinorColor;
    private @FXML TextField timeAxisTextEvery;
    private @FXML ColorPicker timeAxisTextColor;
    private @FXML ChoiceBox<String> timeAxisTextFontFamily;
    private @FXML TextField timeAxisTextValue;
    private @FXML ComboBox<Integer> timeAxisTextSize;
    private @FXML TextField timeAxisTextXOffset;
    private @FXML TextField timeAxisTextYOffset;
    private @FXML CheckBox timeAxisTextBold;
    private @FXML CheckBox timeAxisTextItalic;

    // Fixed Value Axis
    private @FXML CheckBox fixedValueAxis;
    private @FXML TextField fixedValueAxisMajorEvery;
    private @FXML TextField fixedValueAxisMajorWidth;
    private @FXML ColorPicker fixedValueAxisMajorColor;
    private @FXML TextField fixedValueAxisMinorEvery;
    private @FXML TextField fixedValueAxisMinorWidth;
    private @FXML ColorPicker fixedValueAxisMinorColor;
    private @FXML TextField fixedValueAxisTextEvery;
    private @FXML ColorPicker fixedValueAxisTextColor;
    private @FXML ChoiceBox<String> fixedValueAxisTextFontFamily;
    private @FXML TextField fixedValueAxisTextValue;
    private @FXML ComboBox<Integer> fixedValueAxisTextSize;
    private @FXML TextField fixedValueAxisTextXOffset;
    private @FXML TextField fixedValueAxisTextYOffset;
    private @FXML CheckBox fixedValueAxisTextBold;
    private @FXML CheckBox fixedValueAxisTextItalic;

    @Override
    protected void onSceneCreate() {
        super.onSceneCreate();

        // General
        FXUtils.bindSliderToTextField(statsSamplesSlider, statsSamplesField,
                SliderTextFieldType.INTEGER);
        FXUtils.bindSliderToTextField(historyWindowSlider, historyWindowField,
                SliderTextFieldType.INTEGER);
        FXUtils.bindSliderToTextField(pointsSlider, pointsField,
                SliderTextFieldType.INTEGER);
        FXUtils.bindTextFieldToType(FieldType.INTEGER,
                paddingTop, paddingRight, paddingBottom, paddingLeft);
        FXUtils.bindTextFieldToType(FieldType.DECIMAL,
                minValue, maxValue);

        // Visuals
        FXUtils.bindCheckBoxToDisableNode(dots, true, dotSize, dotColor);
        FXUtils.bindCheckBoxToDisableNode(line, true, lineSize, lineColor);
        FXUtils.bindCheckBoxToDisableNode(bars, true, barSize, barColor);
        statistic.getItems().setAll(Statistic.values());
        statistic.setConverter(new StringConverter<Statistic>() {
            @Override
            public String toString(Statistic object) {
                return object.name;
            }

            @Override
            public Statistic fromString(String string) {
                for (Statistic statistic : Statistic.values()) {
                    if (statistic.name.equals(string)) {
                        return statistic;
                    }
                }
                return Statistic.APM;
            }
        });

        // Time Axis
        FXUtils.bindCheckBoxToDisableNode(timeAxis, true, timeAxisMajorEvery,
                timeAxisMajorWidth, timeAxisMajorColor, timeAxisMinorColor, timeAxisMinorEvery,
                timeAxisMinorWidth, timeAxisTextEvery, timeAxisTextColor, timeAxisTextFontFamily,
                timeAxisTextValue, timeAxisTextSize, timeAxisTextXOffset, timeAxisTextYOffset,
                timeAxisTextBold, timeAxisTextItalic);
        FXUtils.bindTextFieldToType(FieldType.INTEGER, timeAxisMajorEvery, timeAxisMajorWidth,
                timeAxisMinorEvery, timeAxisMinorWidth, timeAxisTextEvery, timeAxisTextValue,
                timeAxisTextXOffset, timeAxisTextYOffset);
        timeAxisTextSize.setItems(DEFAULT_TEXT_SIZES);
        timeAxisTextFontFamily.setItems(FXCollections.observableList(Font.getFamilies()));

        // Fixed value axis
        FXUtils.bindCheckBoxToDisableNode(fixedValueAxis, true, fixedValueAxisMajorEvery,
                fixedValueAxisMajorWidth, fixedValueAxisMajorColor, fixedValueAxisMinorColor,
                fixedValueAxisMinorEvery, fixedValueAxisMinorWidth, fixedValueAxisTextEvery,
                fixedValueAxisTextColor, fixedValueAxisTextFontFamily, fixedValueAxisTextValue,
                fixedValueAxisTextSize, fixedValueAxisTextXOffset, fixedValueAxisTextYOffset,
                fixedValueAxisTextBold, fixedValueAxisTextItalic);
        FXUtils.bindTextFieldToType(FieldType.INTEGER, fixedValueAxisMajorEvery,
                fixedValueAxisMajorWidth, fixedValueAxisMinorEvery, fixedValueAxisMinorWidth,
                fixedValueAxisTextEvery, fixedValueAxisTextValue, fixedValueAxisTextXOffset,
                fixedValueAxisTextYOffset);
        fixedValueAxisTextSize.setItems(DEFAULT_TEXT_SIZES);
        fixedValueAxisTextFontFamily.setItems(FXCollections.observableList(Font.getFamilies()));
    }

    @Override
    protected void commitSettings() {
        // General
        
    }

    @Override
    protected void refresh() {
        // General
        statsSamplesField.setText(currentSettings.getStatsBufferSize() + "");
        historyWindowField.setText(currentSettings.getGraphHistoryTimeMS() + "");
        pointsSlider.setValue(currentSettings.getGraphPoints());
        paddingTop.setText(currentSettings.getPaddingTop() + "");
        paddingRight.setText(currentSettings.getPaddingRight() + "");
        paddingBottom.setText(currentSettings.getPaddingBottom() + "");
        paddingLeft.setText(currentSettings.getPaddingLeft() + "");
        backgroundColor.setValue(currentSettings.getBackgroundColor().getColor());
        minValue.setText(currentSettings.getMinValue() + "");
        if (currentSettings.getMinValue() == Long.MIN_VALUE) {
            minValue.setText("0");
        }
        maxValue.setText(currentSettings.getMaxValue() + "");
        if (currentSettings.getMaxValue() == Long.MAX_VALUE) {
            maxValue.setText("0");
        }

        // Visuals
        dots.setSelected(currentSettings.isEnableDots());
        dotSize.setText(currentSettings.getDotSize() + "");
        dotColor.setValue(currentSettings.getDotColor().getColor());
        line.setSelected(currentSettings.isEnableLine());
        lineSize.setText(currentSettings.getLineWidth() + "");
        lineColor.setValue(currentSettings.getLineColor().getColor());
        bars.setSelected(currentSettings.isEnableBars());
        barSize.setText(currentSettings.getBarWidth() + "");
        barColor.setValue(currentSettings.getBarColor().getColor());
        statistic.setValue(currentSettings.getStatistic());

        // Time Axis
        timeAxis.setSelected(currentSettings.getTimeAxis().isEnabled());
        timeAxisMajorEvery.setText(currentSettings.getTimeAxis().getMajorEvery() + "");
        timeAxisMajorWidth.setText(currentSettings.getTimeAxis().getMajorWidth() + "");
        timeAxisMajorColor.setValue(currentSettings.getTimeAxis().getMajorColor().getColor());
        timeAxisMinorEvery.setText(currentSettings.getTimeAxis().getMinorEvery() + "");
        timeAxisMinorWidth.setText(currentSettings.getTimeAxis().getMinorWidth() + "");
        timeAxisMinorColor.setValue(currentSettings.getTimeAxis().getMinorColor().getColor());
        timeAxisTextEvery.setText(currentSettings.getTimeAxis().getTextEvery() + "");
        timeAxisTextColor.setValue(currentSettings.getTimeAxis().getTextColor().getColor());
        timeAxisTextSize.setValue(currentSettings.getTimeAxis().getTextSize());
        timeAxisTextFontFamily.setValue(currentSettings.getTimeAxis().getTextFontFamily());
        timeAxisTextValue.setText(currentSettings.getTimeAxis().getTextValue());
        timeAxisTextXOffset.setText(currentSettings.getTimeAxis().getTextXOffset() + "");
        timeAxisTextYOffset.setText(currentSettings.getTimeAxis().getTextYOffset() + "");
        timeAxisTextBold.setSelected(currentSettings.getTimeAxis().isTextBold());
        timeAxisTextItalic.setSelected(currentSettings.getTimeAxis().isTextItalic());

        // Fixed Value Axis
        fixedValueAxis.setSelected(currentSettings.getFixedValueAxis().isEnabled());
        fixedValueAxisMajorEvery.setText(currentSettings.getFixedValueAxis().getMajorEvery() + "");
        fixedValueAxisMajorWidth.setText(currentSettings.getFixedValueAxis().getMajorWidth() + "");
        fixedValueAxisMajorColor.setValue(currentSettings.getFixedValueAxis().getMajorColor().getColor());
        fixedValueAxisMinorEvery.setText(currentSettings.getFixedValueAxis().getMinorEvery() + "");
        fixedValueAxisMinorWidth.setText(currentSettings.getFixedValueAxis().getMinorWidth() + "");
        fixedValueAxisMinorColor.setValue(currentSettings.getFixedValueAxis().getMinorColor().getColor());
        fixedValueAxisTextEvery.setText(currentSettings.getFixedValueAxis().getTextEvery() + "");
        fixedValueAxisTextColor.setValue(currentSettings.getFixedValueAxis().getTextColor().getColor());
        fixedValueAxisTextSize.setValue(currentSettings.getFixedValueAxis().getTextSize());
        fixedValueAxisTextFontFamily.setValue(currentSettings.getFixedValueAxis().getTextFontFamily());
        fixedValueAxisTextValue.setText(currentSettings.getFixedValueAxis().getTextValue());
        fixedValueAxisTextXOffset.setText(currentSettings.getFixedValueAxis().getTextXOffset() + "");
        fixedValueAxisTextYOffset.setText(currentSettings.getFixedValueAxis().getTextYOffset() + "");
        fixedValueAxisTextBold.setSelected(currentSettings.getFixedValueAxis().isTextBold());
        fixedValueAxisTextItalic.setSelected(currentSettings.getFixedValueAxis().isTextItalic());
    }
}
