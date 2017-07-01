package me.fru1t.streamtools.controller.settings;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import me.fru1t.streamtools.Settings;
import me.fru1t.streamtools.javafx.DefaultSettings;
import me.fru1t.streamtools.util.KeyboardAndMouseStatistics;


/**
 * Settings for {@link me.fru1t.streamtools.controller.GraphStatsController}.
 */
@Data
@Builder
@DefaultSettings("DEFAULT")
@EqualsAndHashCode(callSuper = false)
public class GraphStatsSettings extends Settings<GraphStatsSettings> {
    private static final int DEFAULT_BUFFER_SIZE = KeyboardAndMouseStatistics.DEFAULT_BUFFER_SIZE;
    private static final int DEFAULT_GRAPH_POINTS = 30;
    private static final long DEFAULT_HISTORY_TIME_MS = 1000 * 15; // 1000 ms per second
    private static final int DEFAULT_POINT_SIZE = 5;

    private static final GraphStatsSettings DEFAULT = GraphStatsSettings.builder()
            .statsBufferSize(DEFAULT_BUFFER_SIZE)
            .graphHistoryTimeMS(DEFAULT_HISTORY_TIME_MS)
            .graphPoints(DEFAULT_GRAPH_POINTS)
            .shouldSmooth(false)
            .pointSize(DEFAULT_POINT_SIZE)
            .build();

    // Buffer size is the window of time for averages in the stats mechanism
    private int statsBufferSize;

    // History time is the amount of time (in milliseconds) a stat point should scroll through the
    // entire width of the graph.
    private long graphHistoryTimeMS;

    // The number of datapoints to display on the graph at any one time.
    private int graphPoints;

    // Enable to draw the line as a smooth curve, disable to draw directly from point to point.
    // Smooth looks better for less data points.
    private boolean shouldSmooth;

    private int pointSize;
}
