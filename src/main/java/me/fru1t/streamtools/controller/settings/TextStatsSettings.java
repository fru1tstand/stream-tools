package me.fru1t.streamtools.controller.settings;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import lombok.Builder;
import lombok.Data;
import me.fru1t.streamtools.Settings;
import me.fru1t.streamtools.javafx.DefaultSettings;

import javax.annotation.Nonnull;

/**
 * Settings for the {@link me.fru1t.streamtools.controller.TextStatsController} window.
 */
@Data
@Builder
@DefaultSettings("DEFAULT")
public class TextStatsSettings extends Settings<TextStatsSettings> {
    public static final String ALIGN_LEFT = "left";
    public static final String ALIGN_CENTER = "center";
    public static final String ALIGN_RIGHT = "right";

    public static final int DEFAULT_SIZE = 16;
    private static final String DEFAULT_ALIGN = ALIGN_LEFT;
    private static final boolean DEFAULT_IS_BOLD = false;
    private static final boolean DEFAULT_IS_ITALIC = false;
    private static final Color DEFAULT_COLOR = Color.BLACK;
    private static final Color DEFAULT_BACKGROUND_COLOR = Color.color(0, 1, 0);
    private static final String DEFAULT_CONTENT = "APM: {apm}\nPPM: {ppm}";
    private static final double DEFAULT_WINDOW_HEIGHT = 200;
    private static final double DEFAULT_WINDOW_WIDTH = 300;

    private static final TextStatsSettings DEFAULT = TextStatsSettings.builder()
            .font(Font.getDefault().getFamily())
            .size(DEFAULT_SIZE)
            .align(DEFAULT_ALIGN)
            .isBold(DEFAULT_IS_BOLD)
            .isItalic(DEFAULT_IS_ITALIC)
            .color(DEFAULT_COLOR)
            .backgroundColor(DEFAULT_BACKGROUND_COLOR)
            .content(DEFAULT_CONTENT)
            .build()
            .setWindowHeight(DEFAULT_WINDOW_HEIGHT)
            .setWindowWidth(DEFAULT_WINDOW_WIDTH);

    private @Nonnull String font;
    private int size;
    private @Nonnull String align;
    private boolean isBold;
    private boolean isItalic;
    private @Nonnull Color color;
    private @Nonnull Color backgroundColor;
    private @Nonnull String content;
}
