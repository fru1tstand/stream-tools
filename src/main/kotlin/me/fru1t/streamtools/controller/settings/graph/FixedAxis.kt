package me.fru1t.streamtools.controller.settings.graph

import javafx.scene.paint.Color
import javafx.scene.text.Font
import me.fru1t.javafx.SerializableColor

/** An axis with markers at a fixed constant distance. */
data class FixedAxis(
    var enabled: Boolean = false,

    var firstMajorIncluded: Boolean = false,
    var majorEvery: Long = 0,
    var majorWidth: Int = 1,
    var majorColor: SerializableColor = SerializableColor(Color.BLACK),

    var firstMinorIncluded: Boolean = false,
    var minorEvery: Long = 0,
    var minorWidth: Int = 1,
    var minorColor: SerializableColor = SerializableColor(Color.BLACK),

    var firstTextIncluded: Boolean = false,
    var textEvery: Long = 0,
    var textColor: SerializableColor = SerializableColor(Color.BLACK),
    var textValue: String = "",
    var textFontFamily: String = Font.getDefault().family,
    var textSize: Int = 16,
    var textBold: Boolean = false,
    var textItalic: Boolean = false,
    var textXOffset: Double = 0.0,
    var textYOffset: Double = 0.0
)
