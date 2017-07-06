package me.fru1t.streamtools.controller.settings;

import javafx.scene.text.Font;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.EqualsAndHashCode;
import me.fru1t.streamtools.Settings;
import me.fru1t.streamtools.javafx.DefaultSettings;
import me.fru1t.streamtools.util.KeyboardAndMouseStatistics;

import javax.annotation.Nonnull;

/**
 * Settings for the {@link me.fru1t.streamtools.controller.TextStatsController} window.
 */
@Data
@Builder
@DefaultSettings("DEFAULT")
@EqualsAndHashCode(callSuper = false)
public class TextStatsSettings extends Settings<TextStatsSettings> {
    public static final String ALIGN_LEFT = "left";
    public static final String ALIGN_CENTER = "center";
    public static final String ALIGN_RIGHT = "right";

    public static final int DEFAULT_SIZE = 16;
    private static final String DEFAULT_ALIGN = ALIGN_LEFT;
    private static final boolean DEFAULT_IS_BOLD = false;
    private static final boolean DEFAULT_IS_ITALIC = false;
    private static final String DEFAULT_COLOR = "#000";
    private static final String DEFAULT_BACKGROUND_COLOR = "#0F0";
    private static final String DEFAULT_CONTENT = "APM: {apm}\nPPM: {ppm}";
    private static final int DEFAULT_STATS_BUFFER_SIZE =
            KeyboardAndMouseStatistics.DEFAULT_BUFFER_SIZE;


    // Builder default values are filled by Lombok automatically via the @Default annotation.
    private static final TextStatsSettings DEFAULT = TextStatsSettings.builder().build();


    private @Default @Nonnull String font = Font.getDefault().getFamily();
    private @Default int size = DEFAULT_SIZE;
    private @Default @Nonnull String align = DEFAULT_ALIGN;
    private @Default boolean isBold = DEFAULT_IS_BOLD;
    private @Default boolean isItalic = DEFAULT_IS_ITALIC;
    private @Default @Nonnull String color = DEFAULT_COLOR;
    private @Default @Nonnull String backgroundColor = DEFAULT_BACKGROUND_COLOR;
    private @Default @Nonnull String content = DEFAULT_CONTENT;
    private @Default int statsBufferSize = DEFAULT_STATS_BUFFER_SIZE;
}
