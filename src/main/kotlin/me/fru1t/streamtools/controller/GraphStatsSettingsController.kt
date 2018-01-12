package me.fru1t.streamtools.controller

import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.scene.control.CheckBox
import javafx.scene.control.ChoiceBox
import javafx.scene.control.ColorPicker
import javafx.scene.control.ComboBox
import javafx.scene.control.Slider
import javafx.scene.control.TextField
import javafx.scene.text.Font
import javafx.util.StringConverter
import me.fru1t.javafx.FxmlResource
import me.fru1t.javafx.FxUtils
import me.fru1t.javafx.FxUtils.TextFieldType
import me.fru1t.javafx.FxUtils.SliderTextFieldType
import me.fru1t.streamtools.controller.settings.GraphStatsSettings
import me.fru1t.streamtools.controller.settings.graph.Statistic
import me.fru1t.streamtools.javafx.SettingsController

/** Controller that manages [GraphStatsSettings] for the [GraphStatsController]. */
@FxmlResource("/FXML/GraphStatsSettings.fxml")
class GraphStatsSettingsController : SettingsController<GraphStatsSettings>() {
  // General
  @FXML private lateinit var statsSamplesSlider: Slider
  @FXML private lateinit var statsSamplesField: TextField
  @FXML private lateinit var historyWindowSlider: Slider
  @FXML private lateinit var historyWindowField: TextField
  @FXML private lateinit var pointsSlider: Slider
  @FXML private lateinit var pointsField: TextField
  @FXML private lateinit var paddingTop: TextField
  @FXML private lateinit var paddingLeft: TextField
  @FXML private lateinit var paddingRight: TextField
  @FXML private lateinit var paddingBottom: TextField
  @FXML private lateinit var backgroundColor: ColorPicker
  @FXML private lateinit var minValue: TextField
  @FXML private lateinit var maxValue: TextField

  // Visuals
  @FXML private lateinit var dots: CheckBox
  @FXML private lateinit var dotSize: TextField
  @FXML private lateinit var dotColor: ColorPicker
  @FXML private lateinit var line: CheckBox
  @FXML private lateinit var lineSize: TextField
  @FXML private lateinit var lineColor: ColorPicker
  @FXML private lateinit var bars: CheckBox
  @FXML private lateinit var barSize: TextField
  @FXML private lateinit var barColor: ColorPicker
  @FXML private lateinit var statistic: ChoiceBox<Statistic>

  // Time Axis
  @FXML private lateinit var timeAxis: CheckBox
  @FXML private lateinit var timeAxisMajorEvery: TextField
  @FXML private lateinit var timeAxisMajorWidth: TextField
  @FXML private lateinit var timeAxisMajorColor: ColorPicker
  @FXML private lateinit var timeAxisMinorEvery: TextField
  @FXML private lateinit var timeAxisMinorWidth: TextField
  @FXML private lateinit var timeAxisMinorColor: ColorPicker
  @FXML private lateinit var timeAxisTextEvery: TextField
  @FXML private lateinit var timeAxisTextColor: ColorPicker
  @FXML private lateinit var timeAxisTextFontFamily: ChoiceBox<String>
  @FXML private lateinit var timeAxisTextValue: TextField
  @FXML private lateinit var timeAxisTextSize: ComboBox<Int>
  @FXML private lateinit var timeAxisTextXOffset: TextField
  @FXML private lateinit var timeAxisTextYOffset: TextField
  @FXML private lateinit var timeAxisTextBold: CheckBox
  @FXML private lateinit var timeAxisTextItalic: CheckBox

  // Fixed Value Axis
  @FXML private lateinit var fixedValueAxis: CheckBox
  @FXML private lateinit var fixedValueAxisMajorEvery: TextField
  @FXML private lateinit var fixedValueAxisMajorWidth: TextField
  @FXML private lateinit var fixedValueAxisMajorColor: ColorPicker
  @FXML private lateinit var fixedValueAxisMinorEvery: TextField
  @FXML private lateinit var fixedValueAxisMinorWidth: TextField
  @FXML private lateinit var fixedValueAxisMinorColor: ColorPicker
  @FXML private lateinit var fixedValueAxisTextEvery: TextField
  @FXML private lateinit var fixedValueAxisTextColor: ColorPicker
  @FXML private lateinit var fixedValueAxisTextFontFamily: ChoiceBox<String>
  @FXML private lateinit var fixedValueAxisTextValue: TextField
  @FXML private lateinit var fixedValueAxisTextSize: ComboBox<Int>
  @FXML private lateinit var fixedValueAxisTextXOffset: TextField
  @FXML private lateinit var fixedValueAxisTextYOffset: TextField
  @FXML private lateinit var fixedValueAxisTextBold: CheckBox
  @FXML private lateinit var fixedValueAxisTextItalic: CheckBox

  override fun onSceneCreate() {
    super.onSceneCreate()

    // General
    FxUtils.bindSliderToTextField(statsSamplesSlider, statsSamplesField,
        SliderTextFieldType.INTEGER)
    FxUtils.bindSliderToTextField(historyWindowSlider, historyWindowField,
        SliderTextFieldType.INTEGER)
    FxUtils.bindSliderToTextField(pointsSlider, pointsField,
        SliderTextFieldType.INTEGER)
    FxUtils.bindTextFieldToType(TextFieldType.INTEGER,
        paddingTop, paddingRight, paddingBottom, paddingLeft)
    FxUtils.bindTextFieldToType(TextFieldType.DECIMAL,
        minValue, maxValue)

    // Visuals
    FxUtils.bindCheckBoxToDisableNode(dots, true, dotSize, dotColor)
    FxUtils.bindCheckBoxToDisableNode(line, true, lineSize, lineColor)
    FxUtils.bindCheckBoxToDisableNode(bars, true, barSize, barColor)
    statistic.items.setAll(*Statistic.values())
    statistic.converter = object : StringConverter<Statistic>() {
      override fun toString(`object`: Statistic): String {
        return `object`.displayString
      }

      override fun fromString(string: String): Statistic {
        for (statistic in Statistic.values()) {
          if (statistic.displayString == string) {
            return statistic
          }
        }
        return Statistic.APM
      }
    }

    // Time Axis
    FxUtils.bindCheckBoxToDisableNode(timeAxis, true, timeAxisMajorEvery,
        timeAxisMajorWidth, timeAxisMajorColor, timeAxisMinorColor, timeAxisMinorEvery,
        timeAxisMinorWidth, timeAxisTextEvery, timeAxisTextColor, timeAxisTextFontFamily,
        timeAxisTextValue, timeAxisTextSize, timeAxisTextXOffset, timeAxisTextYOffset,
        timeAxisTextBold, timeAxisTextItalic)
    FxUtils.bindTextFieldToType(TextFieldType.INTEGER, timeAxisMajorEvery, timeAxisMajorWidth,
        timeAxisMinorEvery, timeAxisMinorWidth, timeAxisTextEvery, timeAxisTextValue,
        timeAxisTextXOffset, timeAxisTextYOffset)
    timeAxisTextSize.items = DEFAULT_TEXT_SIZES
    timeAxisTextFontFamily.items = FXCollections.observableList(Font.getFamilies())

    // Fixed value axis
    FxUtils.bindCheckBoxToDisableNode(fixedValueAxis, true, fixedValueAxisMajorEvery,
        fixedValueAxisMajorWidth, fixedValueAxisMajorColor, fixedValueAxisMinorColor,
        fixedValueAxisMinorEvery, fixedValueAxisMinorWidth, fixedValueAxisTextEvery,
        fixedValueAxisTextColor, fixedValueAxisTextFontFamily, fixedValueAxisTextValue,
        fixedValueAxisTextSize, fixedValueAxisTextXOffset, fixedValueAxisTextYOffset,
        fixedValueAxisTextBold, fixedValueAxisTextItalic)
    FxUtils.bindTextFieldToType(TextFieldType.INTEGER, fixedValueAxisMajorEvery,
        fixedValueAxisMajorWidth, fixedValueAxisMinorEvery, fixedValueAxisMinorWidth,
        fixedValueAxisTextEvery, fixedValueAxisTextValue, fixedValueAxisTextXOffset,
        fixedValueAxisTextYOffset)
    fixedValueAxisTextSize.items = DEFAULT_TEXT_SIZES
    fixedValueAxisTextFontFamily.items = FXCollections.observableList(Font.getFamilies())
  }

  override fun commitSettings() {
    // General
    // TODO
  }

  override fun onSettingsChange(settings: GraphStatsSettings) {
    // General
    statsSamplesField.text = currentSettings.statsBufferSize.toString() + ""
    historyWindowField.text = currentSettings.graphHistoryTimeMS.toString() + ""
    pointsSlider.value = currentSettings.graphPoints.toDouble()
    paddingTop.text = currentSettings.paddingTop.toString() + ""
    paddingRight.text = currentSettings.paddingRight.toString() + ""
    paddingBottom.text = currentSettings.paddingBottom.toString() + ""
    paddingLeft.text = currentSettings.paddingLeft.toString() + ""
    backgroundColor.value = currentSettings.backgroundColor.getColor()
    minValue.text = currentSettings.minValue.toString() + ""
    if (currentSettings.minValue == java.lang.Long.MIN_VALUE) {
      minValue.text = "0"
    }
    maxValue.text = currentSettings.maxValue.toString() + ""
    if (currentSettings.maxValue == java.lang.Long.MAX_VALUE) {
      maxValue.text = "0"
    }

    // Visuals
    dots.isSelected = currentSettings.enableDots
    dotSize.text = currentSettings.dotSize.toString() + ""
    dotColor.value = currentSettings.dotColor.getColor()
    line.isSelected = currentSettings.enableLine
    lineSize.text = currentSettings.lineWidth.toString() + ""
    lineColor.value = currentSettings.lineColor.getColor()
    bars.isSelected = currentSettings.enableBars
    barSize.text = currentSettings.barWidth.toString() + ""
    barColor.value = currentSettings.barColor.getColor()
    statistic.value = currentSettings.statistic

    // Time Axis
    timeAxis.isSelected = currentSettings.timeAxis.enabled
    timeAxisMajorEvery.text = currentSettings.timeAxis.majorEvery.toString() + ""
    timeAxisMajorWidth.text = currentSettings.timeAxis.majorWidth.toString() + ""
    timeAxisMajorColor.value = currentSettings.timeAxis.majorColor.getColor()
    timeAxisMinorEvery.text = currentSettings.timeAxis.minorEvery.toString() + ""
    timeAxisMinorWidth.text = currentSettings.timeAxis.minorWidth.toString() + ""
    timeAxisMinorColor.value = currentSettings.timeAxis.minorColor.getColor()
    timeAxisTextEvery.text = currentSettings.timeAxis.textEvery.toString() + ""
    timeAxisTextColor.value = currentSettings.timeAxis.textColor.getColor()
    timeAxisTextSize.value = currentSettings.timeAxis.textSize
    timeAxisTextFontFamily.value = currentSettings.timeAxis.textFontFamily
    timeAxisTextValue.text = currentSettings.timeAxis.textValue
    timeAxisTextXOffset.text = currentSettings.timeAxis.textXOffset.toString() + ""
    timeAxisTextYOffset.text = currentSettings.timeAxis.textYOffset.toString() + ""
    timeAxisTextBold.isSelected = currentSettings.timeAxis.textBold
    timeAxisTextItalic.isSelected = currentSettings.timeAxis.textItalic

    // Fixed Value Axis
    fixedValueAxis.isSelected = currentSettings.fixedValueAxis.enabled
    fixedValueAxisMajorEvery.text = currentSettings.fixedValueAxis.majorEvery.toString() + ""
    fixedValueAxisMajorWidth.text = currentSettings.fixedValueAxis.majorWidth.toString() + ""
    fixedValueAxisMajorColor.value = currentSettings.fixedValueAxis.majorColor.getColor()
    fixedValueAxisMinorEvery.text = currentSettings.fixedValueAxis.minorEvery.toString() + ""
    fixedValueAxisMinorWidth.text = currentSettings.fixedValueAxis.minorWidth.toString() + ""
    fixedValueAxisMinorColor.value = currentSettings.fixedValueAxis.minorColor.getColor()
    fixedValueAxisTextEvery.text = currentSettings.fixedValueAxis.textEvery.toString() + ""
    fixedValueAxisTextColor.value = currentSettings.fixedValueAxis.textColor.getColor()
    fixedValueAxisTextSize.value = currentSettings.fixedValueAxis.textSize
    fixedValueAxisTextFontFamily.value = currentSettings.fixedValueAxis.textFontFamily
    fixedValueAxisTextValue.text = currentSettings.fixedValueAxis.textValue
    fixedValueAxisTextXOffset.text = currentSettings.fixedValueAxis.textXOffset.toString() + ""
    fixedValueAxisTextYOffset.text = currentSettings.fixedValueAxis.textYOffset.toString() + ""
    fixedValueAxisTextBold.isSelected = currentSettings.fixedValueAxis.textBold
    fixedValueAxisTextItalic.isSelected = currentSettings.fixedValueAxis.textItalic
  }

  companion object {
    private val DEFAULT_TEXT_SIZES = FXCollections.observableArrayList(
        1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 14, 16, 18, 22, 26, 32, 64, 72)
  }
}
