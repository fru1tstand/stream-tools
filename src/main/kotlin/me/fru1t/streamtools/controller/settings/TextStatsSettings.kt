package me.fru1t.streamtools.controller.settings

import javafx.scene.paint.Color
import javafx.scene.text.Font
import me.fru1t.javafx.FxUtils
import me.fru1t.streamtools.controller.Settings
import me.fru1t.streamtools.util.KeyboardAndMouseStatistics

/** Settings for the [me.fru1t.streamtools.controller.TextStatsController] window. */
data class TextStatsSettings(
    var font: String? = Font.getDefault().family,
    var size: Int = DEFAULT_SIZE,
    var align: String = ALIGN_LEFT,
    var isBold: Boolean = false,
    var isItalic: Boolean = false,
    var color: String = FxUtils.colorToHex(Color.BLACK),
    var backgroundColor: String = FxUtils.colorToHex(Color.GREEN),
    var content: String = "APM: {apm}\nPPM: {ppm}",
    var statsBufferSize: Int = KeyboardAndMouseStatistics.DEFAULT_BUFFER_SIZE
) : Settings<TextStatsSettings>() {
  companion object {
    // TODO: Change to enum. wtf are you thinking
    val ALIGN_LEFT = "left"
    val ALIGN_CENTER = "center"
    val ALIGN_RIGHT = "right"

    val DEFAULT_SIZE = 16
  }
}
