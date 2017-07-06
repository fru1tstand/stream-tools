package me.fru1t.streamtools.controller.settings;

import javafx.scene.paint.Color;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.EqualsAndHashCode;
import me.fru1t.javafx.FXUtils;
import me.fru1t.streamtools.Settings;
import me.fru1t.streamtools.javafx.DefaultSettings;
import me.fru1t.streamtools.util.KeyboardAndMouseStatistics;

import javax.annotation.Nonnull;


/**
 * Settings for {@link me.fru1t.streamtools.controller.GraphStatsController}.
 */
@Data
@Builder
@DefaultSettings("DEFAULT")
@EqualsAndHashCode(callSuper = false)
public class GraphStatsSettings extends Settings<GraphStatsSettings> {
    private static final int DEFAULT_STATS_BUFFER_SIZE =
            KeyboardAndMouseStatistics.DEFAULT_BUFFER_SIZE;
    private static final int DEFAULT_GRAPH_POINTS = 30;
    private static final long DEFAULT_HISTORY_TIME_MS = 1000 * 15; // 1000 ms per second
    private static final int DEFAULT_PADDING = 5;

    private static final boolean DEFAULT_ENABLE_DOTS = true;
    private static final int DEFAULT_DOT_SIZE = 20;
    private static final String DEFAULT_DOT_COLOR = FXUtils.colorToHex(Color.BLACK);

    private static final boolean DEFAULT_ENABLE_LINE = true;
    private static final int DEFAULT_LINE_WIDTH = 2;
    private static final String DEFAULT_LINE_COLOR = FXUtils.colorToHex(Color.YELLOW);

    // Lombok automatically fills in default field values via the @Default annotation.
    private static final GraphStatsSettings DEFAULT = GraphStatsSettings.builder().build();


    // Buffer size is the window of time for averages in the stats mechanism
    private @Default int statsBufferSize = DEFAULT_STATS_BUFFER_SIZE;

    // History time is the amount of time (in milliseconds) a stat point should scroll through the
    // entire width of the graph.
    private @Default long graphHistoryTimeMS = DEFAULT_HISTORY_TIME_MS;

    // The number of datapoints to display on the graph at any one time.
    private @Default int graphPoints = DEFAULT_GRAPH_POINTS;

    private @Default int paddingTop = DEFAULT_PADDING;
    private @Default int paddingRight = DEFAULT_PADDING;
    private @Default int paddingBottom = DEFAULT_PADDING;
    private @Default int paddingLeft = DEFAULT_PADDING;

    // ** Dots
    private @Default boolean enableDots = DEFAULT_ENABLE_DOTS;
    private @Default int dotSize = DEFAULT_DOT_SIZE;
    private @Default @Nonnull String dotColor = DEFAULT_DOT_COLOR; // In hex string form.

    // ** Line
    private @Default boolean enableLine = DEFAULT_ENABLE_LINE;
    private @Default int lineWidth = DEFAULT_LINE_WIDTH;
    private @Default @Nonnull String lineColor = DEFAULT_LINE_COLOR; // In hex string form.
}
