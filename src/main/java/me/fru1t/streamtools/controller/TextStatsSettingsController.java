package me.fru1t.streamtools.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.StringConverter;
import me.fru1t.javafx.FxmlResource;
import me.fru1t.javafx.FxUtils;
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
@FxmlResource("/FXML/TextStatsSettings.fxml")
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
                    return TextStatsSettings.Companion.getDEFAULT_SIZE();
                }
            }
        });

        // Combo box quirkiness
        size.getEditor().focusedProperty().addListener((observable, wasFocused, isNowFocused) -> {
            Integer newValue = TextStatsSettings.Companion.getDEFAULT_SIZE();
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
        getCurrentSettings().setFont(fontName.getValue());
        getCurrentSettings().setSize(size.getValue());
        getCurrentSettings().setBold(bold.isSelected());
        getCurrentSettings().setItalic(italic.isSelected());
        getCurrentSettings().setColor(FxUtils.INSTANCE.colorToHex(textColor.getValue()));
        getCurrentSettings().setBackgroundColor(FxUtils.INSTANCE.colorToHex(backgroundColor.getValue()));
        getCurrentSettings().setContent(content.getText());

        getCurrentSettings().setAlign(TextStatsSettings.Companion.getALIGN_LEFT());
        if (centerJustify.isSelected()) {
            getCurrentSettings().setAlign(TextStatsSettings.Companion.getALIGN_CENTER());
        } else if (rightJustify.isSelected()) {
            getCurrentSettings().setAlign(TextStatsSettings.Companion.getALIGN_RIGHT());
        }
    }

    @Override
    protected void onSettingsChange() {
        fontName.setValue(getCurrentSettings().getFont());
        size.setValue(getCurrentSettings().getSize());
        bold.setSelected(getCurrentSettings().isBold());
        italic.setSelected(getCurrentSettings().isItalic());
        textColor.setValue(Color.web(getCurrentSettings().getColor()));
        backgroundColor.setValue(Color.web(getCurrentSettings().getBackgroundColor()));
        content.setText(getCurrentSettings().getContent());

        leftJustify.setSelected(false);
        centerJustify.setSelected(false);
        rightJustify.setSelected(false);
        if (getCurrentSettings().getAlign().equals(TextStatsSettings.Companion.getALIGN_CENTER())) {
            centerJustify.setSelected(true);
        } else if (getCurrentSettings().getAlign().equals(TextStatsSettings.Companion.getALIGN_LEFT())) {
            leftJustify.setSelected(true);
        } else {
            rightJustify.setSelected(true);
        }
    }
}
