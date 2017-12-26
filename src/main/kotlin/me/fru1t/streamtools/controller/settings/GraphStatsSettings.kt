package me.fru1t.streamtools.controller.settings

import javafx.scene.paint.Color
import me.fru1t.javafx.FXUtils
import me.fru1t.streamtools.Settings
import me.fru1t.streamtools.util.KeyboardAndMouseStatistics

/** Settings for [me.fru1t.streamtools.controller.GraphStatsController]. */
data class GraphStatsSettings(
    // Buffer size is the window of time for averages in the stats mechanism
    var statsBufferSize: Int = KeyboardAndMouseStatistics.DEFAULT_BUFFER_SIZE,

    // History time is the amount of time (in milliseconds) a stat point should scroll through the
    // entire width of the graph.
    var graphHistoryTimeMS: Long = (1000 * 15).toLong(), // 1000 ms per second,

    // The number of datapoints to display on the graph at any one time.
    var graphPoints: Int = 30,

    var paddingTop: Int = 5,
    var paddingRight: Int = 5,
    var paddingBottom: Int = 5,
    var paddingLeft: Int = 5,

    // ** Dots
    var enableDots: Boolean = true,
    var dotSize: Int = 20,
    var dotColor: String = FXUtils.colorToHex(Color.BLACK), // In hex string form.

    // ** Line
    var enableLine: Boolean = true,
    var lineWidth: Int = 2,
    var lineColor: String = FXUtils.colorToHex(Color.YELLOW) // In hex string form.
) : Settings<GraphStatsSettings>()
