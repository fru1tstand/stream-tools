package me.fru1t.streamtools.controller

import javafx.fxml.FXML
import javafx.geometry.HPos
import javafx.scene.control.Label
import javafx.scene.layout.GridPane
import me.fru1t.javafx.FxmlResource
import me.fru1t.slik.Slik
import me.fru1t.streamtools.StreamToolsApplication
import me.fru1t.streamtools.controller.settings.TextStatsSettings
import me.fru1t.streamtools.javafx.WindowWithSettingsController
import me.fru1t.streamtools.util.KeyboardAndMouseStatisticsFactory

/**
 * Text stats show a text-only window that shows customizable statistics. Use
 * [me.fru1t.javafx.Controller.create] to instantiate.
 */
@FxmlResource("/FXML/TextStats.fxml")
class TextStatsController :
    WindowWithSettingsController<TextStatsSettings, TextStatsSettingsController>() {
  private val scope = Slik.get(StreamToolsApplication::class)
  private val stats = scope.inject<KeyboardAndMouseStatisticsFactory>().create()

  @FXML
  private lateinit var textStatsLabel: Label

  private var content: String = ""

  override fun onUpdate(now: Long) {
    stats.tick()
    val (keyboardAPM, mousePPM, totalActions, totalPixels) = stats.getCurrentData()
    var content = this.content

    val totalKeypresses = TOTAL_KEYPRESS_REGEX.findAll(content)
    totalKeypresses.forEach {
      content = content.replace(
          it.value,
          stats.getTotalKeyPresses(it.groupValues[1][0]).toString())
    }

    textStatsLabel.text =
        content.replace(ACTIONS_PER_MINUTE_PLACEHOLDER, keyboardAPM.toString())
            .replace(PIXELS_PER_MINUTE_PLACEHOLDER, mousePPM.toString())
            .replace(TOTAL_ACTIONS_PLACEHOLDER, totalActions.toString())
            .replace(TOTAL_PIXELS_PLACEHOLDER, totalPixels.toString())
  }

  override fun onSceneCreate() {
    super.onSceneCreate()
    GridPane.setFillWidth(textStatsLabel, true)
  }

  override fun shutdown() {
    stats.shutdown()
    super.shutdown()
  }

  override fun onSettingsChange(settings: TextStatsSettings) {
    textStatsLabel.style =
        String.format(
            TEXT_STATS_LABEL_STYLE,
            settings.font,
            settings.size,
            settings.align,
            if (settings.isBold) "bold" else "normal",
            if (settings.isItalic) "italic" else "normal",
            settings.color)

    // TODO: Replace with enum
    if (settings.align == TextStatsSettings.ALIGN_CENTER) {
      GridPane.setHalignment(textStatsLabel, HPos.CENTER)
    } else if (settings.align == TextStatsSettings.ALIGN_LEFT) {
      GridPane.setHalignment(textStatsLabel, HPos.LEFT)
    } else {
      GridPane.setHalignment(textStatsLabel, HPos.RIGHT)
    }

    scene.root.style = String.format(ROOT_STYLE, settings.backgroundColor)
    content = settings.content
    stats.bufferSize = settings.statsBufferSize
  }

  companion object {
    private val ACTIONS_PER_MINUTE_PLACEHOLDER = "{apm}"
    private val PIXELS_PER_MINUTE_PLACEHOLDER = "{ppm}"
    private val TOTAL_ACTIONS_PLACEHOLDER = "{ta}"
    private val TOTAL_PIXELS_PLACEHOLDER = "{tp}"
    private val TOTAL_KEYPRESS_REGEX = "\\{tk:(.)}".toRegex()

    private val TEXT_STATS_LABEL_STYLE =
        "-fx-font-family: \"%s\"; " +
            "-fx-font-size: %dpx; " +
            "-fx-text-alignment: %s; " +
            "-fx-font-weight: %s; " +
            "-fx-font-style: %s; " +
            "-fx-text-fill: %s; "
    private val ROOT_STYLE = "-fx-background-color: %s;"
  }
}
