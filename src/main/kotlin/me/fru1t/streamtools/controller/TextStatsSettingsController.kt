package me.fru1t.streamtools.controller

import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.scene.control.CheckBox
import javafx.scene.control.ChoiceBox
import javafx.scene.control.ColorPicker
import javafx.scene.control.ComboBox
import javafx.scene.control.TextArea
import javafx.scene.control.ToggleButton
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.util.StringConverter
import me.fru1t.javafx.FxmlResource
import me.fru1t.javafx.FxUtils
import me.fru1t.streamtools.controller.settings.TextStatsSettings
import me.fru1t.streamtools.javafx.SettingsController
import org.controlsfx.glyphfont.FontAwesome
import org.controlsfx.glyphfont.GlyphFontRegistry

import java.util.logging.Level
import java.util.logging.Logger

/** A [SettingsController] that manages [TextStatsSettings] for [TextStatsController]. */
@FxmlResource("/FXML/TextStatsSettings.fxml")
class TextStatsSettingsController : SettingsController<TextStatsSettings>() {

  @FXML private lateinit var leftJustify: ToggleButton
  @FXML private lateinit var centerJustify: ToggleButton
  @FXML private lateinit var rightJustify: ToggleButton

  @FXML private lateinit var fontName: ChoiceBox<String>
  @FXML private lateinit var size: ComboBox<Int>
  @FXML private lateinit var bold: CheckBox
  @FXML private lateinit var italic: CheckBox
  @FXML private lateinit var textColor: ColorPicker
  @FXML private lateinit var backgroundColor: ColorPicker
  @FXML private lateinit var content: TextArea

  public override fun onSceneCreate() {
    super.onSceneCreate()

    // Apply font awesome glyphs to text align buttons
    val font = GlyphFontRegistry.font("FontAwesome")
    leftJustify.graphic = font.create(FontAwesome.Glyph.ALIGN_LEFT)
    centerJustify.graphic = font.create(FontAwesome.Glyph.ALIGN_CENTER)
    rightJustify.graphic = font.create(FontAwesome.Glyph.ALIGN_RIGHT)

    // Load fonts
    fontName.items = FXCollections.observableList(Font.getFamilies())

    // Default font sizes
    size.items = FXCollections.observableArrayList(*FONT_SIZE_LIST)
    size.converter = object : StringConverter<Int>() {
      override fun toString(`object`: Int?): String {
        return `object`!!.toString() + ""
      }

      override fun fromString(string: String): Int? {
        try {
          return Integer.parseInt(string)
        } catch (e: Exception) {
          LOGGER.log(Level.WARNING, e.message)
          return TextStatsSettings.DEFAULT_SIZE
        }

      }
    }

    // Combo box quirkiness
    size.editor.focusedProperty().addListener { _, _, isNowFocused ->
      var newValue: Int? = TextStatsSettings.DEFAULT_SIZE
      try {
        newValue = Integer.parseInt(size.editor.text)
      } catch (e: NumberFormatException) {
        LOGGER.log(Level.INFO, "User typed an invalid font size, so we're defaulting to $newValue")
      }

      if (!isNowFocused) {
        size.value = newValue
      }
    }
  }

  override fun commitSettings() {
    currentSettings.font = fontName.value
    currentSettings.size = size.value
    currentSettings.isBold = bold.isSelected
    currentSettings.isItalic = italic.isSelected
    currentSettings.color = FxUtils.colorToHex(textColor.value)
    currentSettings.backgroundColor = FxUtils.colorToHex(backgroundColor.value)
    currentSettings.content = content.text

    currentSettings.align = TextStatsSettings.ALIGN_LEFT
    if (centerJustify.isSelected) {
      currentSettings.align = TextStatsSettings.ALIGN_CENTER
    } else if (rightJustify.isSelected) {
      currentSettings.align = TextStatsSettings.ALIGN_RIGHT
    }
  }

  override fun onSettingsChange() {
    fontName.value = currentSettings.font
    size.value = currentSettings.size
    bold.isSelected = currentSettings.isBold
    italic.isSelected = currentSettings.isItalic
    textColor.value = Color.web(currentSettings.color)
    backgroundColor.value = Color.web(currentSettings.backgroundColor)
    content.text = currentSettings.content

    leftJustify.isSelected = false
    centerJustify.isSelected = false
    rightJustify.isSelected = false
    when (currentSettings.align) {
      TextStatsSettings.ALIGN_CENTER -> centerJustify.isSelected = true
      TextStatsSettings.ALIGN_LEFT -> leftJustify.isSelected = true
      else -> rightJustify.isSelected = true
    }
  }

  companion object {
    private val LOGGER = Logger.getLogger(TextStatsSettingsController::class.java.name)

    private val FONT_SIZE_LIST = arrayOf(5, 8, 10, 12, 14, 16, 18, 22, 26, 32, 48, 54, 72)
  }
}
