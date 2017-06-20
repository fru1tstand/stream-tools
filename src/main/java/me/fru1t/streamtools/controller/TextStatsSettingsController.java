package me.fru1t.streamtools.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import me.fru1t.javafx.FXMLResource;
import me.fru1t.streamtools.controller.settings.TextStatsSettings;
import me.fru1t.streamtools.javafx.SettingsController;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.GlyphFont;
import org.controlsfx.glyphfont.GlyphFontRegistry;

import javax.annotation.Nonnull;

@FXMLResource("/FXML/TextStatsSettings.fxml")
public class TextStatsSettingsController extends SettingsController<TextStatsSettings> {

    private @FXML ToggleButton leftJustify;
    private @FXML ToggleButton centerJustify;
    private @FXML ToggleButton rightJustify;

    @Override
    public void onSceneCreate() {
        super.onSceneCreate();

        // Load fonts
        GlyphFont font = GlyphFontRegistry.font("FontAwesome");
        leftJustify.setGraphic(font.create(FontAwesome.Glyph.ALIGN_LEFT));
        centerJustify.setGraphic(font.create(FontAwesome.Glyph.ALIGN_JUSTIFY));
        rightJustify.setGraphic(font.create(FontAwesome.Glyph.ALIGN_RIGHT));

        // TODO: Does this do anything?
        ToggleGroup group = new ToggleGroup();
        leftJustify.setToggleGroup(group);
        centerJustify.setToggleGroup(group);
        rightJustify.setToggleGroup(group);
    }

    @Nonnull
    @Override
    protected TextStatsSettings getSettings() {
        return null;
    }

    @Override
    protected void reset() {

    }

    @Override
    protected void reset(TextStatsSettings settings) {

    }
}
