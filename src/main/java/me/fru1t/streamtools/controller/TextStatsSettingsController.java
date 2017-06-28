package me.fru1t.streamtools.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.StringConverter;
import me.fru1t.javafx.FXMLResource;
import me.fru1t.javafx.FXUtils;
import me.fru1t.streamtools.controller.settings.TextStatsSettings;
import me.fru1t.streamtools.javafx.SettingsController;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.GlyphFont;
import org.controlsfx.glyphfont.GlyphFontRegistry;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the TextStatsSettings window. This ultimately controls the TextStats window.
 */
@FXMLResource("/FXML/TextStatsSettings.fxml")
public class TextStatsSettingsController extends SettingsController<TextStatsSettings> {
    private static final Logger LOGGER =
            Logger.getLogger(TextStatsSettingsController.class.getName());

    private static final Integer[] FONT_SIZE_LIST = new Integer[] {
            5, 8, 10, 12, 14, 16, 18, 22, 26, 32, 48, 54, 72};

    private @FXML ToggleButton leftJustify;
    private @FXML ToggleButton centerJustify;
    private @FXML ToggleButton rightJustify;

    private @FXML ChoiceBox<String> fontName;
    private @FXML ComboBox<Integer> size;
    private @FXML CheckBox bold;
    private @FXML CheckBox italic;
    private @FXML ColorPicker textColor;
    private @FXML ColorPicker backgroundColor;
    private @FXML TextArea content;

    @Override
    public void onSceneCreate() {
        super.onSceneCreate();

        // Apply font awesome glyphs to text align buttons
        GlyphFont font = GlyphFontRegistry.font("FontAwesome");
        leftJustify.setGraphic(font.create(FontAwesome.Glyph.ALIGN_LEFT));
        centerJustify.setGraphic(font.create(FontAwesome.Glyph.ALIGN_CENTER));
        rightJustify.setGraphic(font.create(FontAwesome.Glyph.ALIGN_RIGHT));

        // Load fonts
        fontName.setItems(FXCollections.observableList(Font.getFamilies()));

        // Default font sizes
        size.setItems(FXCollections.observableArrayList(FONT_SIZE_LIST));
        size.setConverter(new StringConverter<Integer>() {
            @Override
            public String toString(Integer object) {
                return object + "";
            }

            @Override
            public Integer fromString(String string) {
                try {
                    return Integer.parseInt(string);
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, e.getMessage());
                    return TextStatsSettings.DEFAULT_SIZE;
                }
            }
        });

        // Combo box quirkiness
        size.getEditor().focusedProperty().addListener((observable, wasFocused, isNowFocused) -> {
            Integer newValue = TextStatsSettings.DEFAULT_SIZE;
            try {
                newValue = Integer.parseInt(size.getEditor().getText());
            } catch (NumberFormatException e) {
                LOGGER.log(Level.INFO, "User typed an invalid font size, so we're defaulting "
                        + "to " + newValue);
            }

            if (!isNowFocused) {
                size.setValue(newValue);
            }
        });
    }

    @Override
    protected void commitSettings() {
        currentSettings.setFont(fontName.getValue());
        currentSettings.setSize(size.getValue());
        currentSettings.setBold(bold.isSelected());
        currentSettings.setItalic(italic.isSelected());
        currentSettings.setColor(FXUtils.colorToHex(textColor.getValue()));
        currentSettings.setBackgroundColor(FXUtils.colorToHex(backgroundColor.getValue()));
        currentSettings.setContent(content.getText());

        currentSettings.setAlign(TextStatsSettings.ALIGN_LEFT);
        if (centerJustify.isSelected()) {
            currentSettings.setAlign(TextStatsSettings.ALIGN_CENTER);
        } else if (rightJustify.isSelected()) {
            currentSettings.setAlign(TextStatsSettings.ALIGN_RIGHT);
        }
    }

    @Override
    protected void refresh() {
        fontName.setValue(currentSettings.getFont());
        size.setValue(currentSettings.getSize());
        bold.setSelected(currentSettings.isBold());
        italic.setSelected(currentSettings.isItalic());
        textColor.setValue(Color.web(currentSettings.getColor()));
        backgroundColor.setValue(Color.web(currentSettings.getBackgroundColor()));
        content.setText(currentSettings.getContent());

        leftJustify.setSelected(false);
        centerJustify.setSelected(false);
        rightJustify.setSelected(false);
        switch (currentSettings.getAlign()) {
            case TextStatsSettings.ALIGN_CENTER:
                centerJustify.setSelected(true);
                break;
            case TextStatsSettings.ALIGN_LEFT:
                leftJustify.setSelected(true);
                break;
            case TextStatsSettings.ALIGN_RIGHT:
                rightJustify.setSelected(true);
                break;
        }
    }
}
