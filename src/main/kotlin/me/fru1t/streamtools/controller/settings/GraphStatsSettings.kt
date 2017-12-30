package me.fru1t.streamtools.controller.settings

import javafx.scene.paint.Color
import javafx.scene.text.Font
import me.fru1t.javafx.FXUtils
import me.fru1t.javafx.SerializableColor
import me.fru1t.streamtools.Settings
import me.fru1t.streamtools.controller.settings.graph.FixedAxis
import me.fru1t.streamtools.controller.settings.graph.Statistic
import me.fru1t.streamtools.util.KeyboardAndMouseStatistics

/** Settings for [me.fru1t.streamtools.controller.GraphStatsController]. */
data class GraphStatsSettings(
    // ** General
    var statsBufferSize: Int = KeyboardAndMouseStatistics.DEFAULT_BUFFER_SIZE,
    var graphHistoryTimeMS: Long = (1000 * 15).toLong(), // 1000 ms per second,
    var graphPoints: Int = 200,
    var backgroundColor: SerializableColor = SerializableColor("#0f0"),

    var minValue: Long = 230,
    var maxValue: Long = Long.MAX_VALUE,

    var paddingTop: Int = 5,
    var paddingRight: Int = 5,
    var paddingBottom: Int = 5,
    var paddingLeft: Int = 5,

    // ** Time Axis
    var timeAxis: FixedAxis = FixedAxis(
        enabled = true,
        majorEvery = 5000,
        majorWidth = 1,
        majorColor = SerializableColor("#ccc"),

        minorEvery = 1000,
        minorWidth = 1,
        minorColor = SerializableColor("#eee"),

        textEvery = 5000,
        textColor = SerializableColor("#000"),
        textValue = "+{s}s",
        textFontFamily = Font.getDefault().family,
        textSize = 10,
        textBold = false,
        textItalic = false,
        textXOffset = 3.0,
        textYOffset = 0.0),
    
    // ** Fixed value axis
    var fixedValueAxis: FixedAxis = FixedAxis(
        enabled = true,
        majorEvery = 50,
        majorWidth = 1,
        majorColor = SerializableColor("#ccc"),

        minorEvery = 25,
        minorWidth = 1,
        minorColor = SerializableColor("#eee"),

        textEvery = 50,
        textColor = SerializableColor("#000"),
        textValue = "{v}apm",
        textFontFamily = Font.getDefault().family,
        textSize = 10,
        textBold = false,
        textItalic = false,
        textXOffset = 3.0,
        textYOffset = -3.0),

    // ** Visuals
    var statistic: Statistic = Statistic.APM,

    // ** Dots
    var enableDots: Boolean = true,
    var dotSize: Int = 8,
    var dotColor: SerializableColor = SerializableColor(Color.BLACK),

    // ** Line
    var enableLine: Boolean = true,
    var lineWidth: Int = 2,
    var lineColor: SerializableColor = SerializableColor(Color.AQUA),

    var enableBars: Boolean = false,
    var barWidth: Int = 10,
    var barColor: SerializableColor = SerializableColor(Color.WHEAT)
) : Settings<GraphStatsSettings>() {
  companion object {
    /** A placeholder for the number of milliseconds since the start of the graph. */
    val TIME_AXIS_VALUE_MS = "{ms}"

    /** A placeholder for the number of seconds since the start of the graph, rounded. */
    val TIME_AXIS_VALUE_S = "{s}"

    /** A placeholder for the value shown in the graph. */
    val VALUE_AXIS_VALUE = "{v}"
  }
}
