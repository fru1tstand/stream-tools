package me.fru1t.streamtools.controller

import javafx.fxml.FXML
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.layout.AnchorPane
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.FontPosture
import javafx.scene.text.FontWeight
import me.fru1t.javafx.FxmlResource
import me.fru1t.streamtools.controller.settings.GraphStatsSettings
import me.fru1t.streamtools.javafx.WindowWithSettingsController
import me.fru1t.streamtools.util.KeyboardAndMouseStatistics

/** Shows statistics in graph form. */
@FxmlResource("/FXML/GraphStats.fxml")
class GraphStatsController : WindowWithSettingsController<GraphStatsSettings, GraphStatsSettingsController>() {

  @FXML private lateinit var canvas: Canvas
  private lateinit var ctx: GraphicsContext
  private val stats: KeyboardAndMouseStatistics = KeyboardAndMouseStatistics()

  // Settings
  private lateinit var settings: GraphStatsSettings

  private lateinit var data: LongArray
  private var dataPointer: Int = 0

  private var lastTime: Long = 0
  private var msSinceLastPoint: Long = 0

  // Values set by settings or canvas size
  @Transient private var pixelsPerMillisecond: Double = 0.0
  @Transient private var spaceBetweenPoints: Double = 0.0
  @Transient private var msBetweenPoints: Long = 0

  @Transient private var timeAxisPxBetweenMajor: Double = 0.0
  @Transient private var timeAxisPxBetweenMinor: Double = 0.0
  @Transient private var timeAxisPxBetweenText: Double = 0.0

  override fun onSceneCreate() {
    super.onSceneCreate()
    ctx = canvas.graphicsContext2D

    // Set up canvas resizing
    scene.widthProperty().addListener { _, _, _ -> updateCanvasSize() }
    scene.heightProperty().addListener { _, _, _ -> updateCanvasSize() }

    canvas.style = CANVAS_STYLE
  }

  override fun onUpdate(now: Long) {
    // Calculate some time information
    msSinceLastPoint += (now - lastTime) / 1000000
    lastTime = now
    stats.tick()

    // Check if we should add a point
    if (msSinceLastPoint >= msBetweenPoints) {
      val (keyboardAPM) = stats.getCurrentData()
      msSinceLastPoint = 0
      data[dataPointer] = keyboardAPM.toLong()
      dataPointer = (dataPointer + 1) % data.size
    }

    // Find largest value
    var largestData = settings.minValue
    for (d in data) {
      if (d > settings.maxValue) {
        largestData = settings.maxValue
        break
      }
      if (d > largestData) {
        largestData = d
      }
    }

    ctx.clearRect(0.0, 0.0, canvas.width, canvas.height)
    ctx.fill = Color.BLACK

    // Time Axis
    if (settings.timeAxis.enabled) {
      var currentPosition = canvas.width

      // Major
      if (timeAxisPxBetweenMajor > 1) {
        ctx.fill = settings.timeAxis.majorColor.getColor()
        if (!settings.timeAxis.firstMajorIncluded) {
          currentPosition -= timeAxisPxBetweenMajor
        }
        while (currentPosition > 0) {
          ctx.fillRect(currentPosition, 0.0, settings.timeAxis.majorWidth.toDouble(), canvas.height)
          currentPosition -= timeAxisPxBetweenMajor
        }
      }

      if (timeAxisPxBetweenMinor > 0) {
        currentPosition = canvas.width
        ctx.fill = settings.timeAxis.minorColor.getColor()
        if (!settings.timeAxis.firstMajorIncluded) {
          currentPosition -= timeAxisPxBetweenMinor
        }
        while (currentPosition > 0) {
          ctx.fillRect(currentPosition, 0.0, settings.timeAxis.minorWidth.toDouble(), canvas.height)
          currentPosition -= timeAxisPxBetweenMinor
        }
      }

      if (timeAxisPxBetweenText > 0) {
        currentPosition = canvas.width
        var currentTime: Long = 0
        ctx.font =
            Font.font(
                settings.timeAxis.textFontFamily,
                if (settings.timeAxis.textBold) FontWeight.BOLD else FontWeight.NORMAL,
                if (settings.timeAxis.textItalic) FontPosture.ITALIC else FontPosture.REGULAR,
                settings.timeAxis.textSize.toDouble())
        ctx.fill = settings.timeAxis.textColor.getColor()
        if (!settings.timeAxis.firstTextIncluded) {
          currentTime += settings.timeAxis.textEvery
          currentPosition -= timeAxisPxBetweenText
        }
        while (currentPosition > 0) {
          ctx.fillText(
              parseTimeText(currentTime, settings.timeAxis.textValue),
              currentPosition + settings.timeAxis.textXOffset,
              canvas.height - settings.timeAxis.textYOffset)
          currentTime += settings.timeAxis.textEvery
          currentPosition -= timeAxisPxBetweenText
        }
      }
    }

    // Fixed value axis
    if (settings.fixedValueAxis.enabled) {
      var currentPosition = canvas.height

      // Major
      if (settings.fixedValueAxis.majorEvery > 1) {
        ctx.fill = settings.fixedValueAxis.majorColor.getColor()
        val pxPerLine = canvas.height / (1.0 * largestData / settings.fixedValueAxis.majorEvery)
        if (!settings.fixedValueAxis.firstMajorIncluded) {
          currentPosition -= pxPerLine
        }
        while (currentPosition > 0) {
          ctx.fillRect(
              0.0, currentPosition, canvas.width, settings.fixedValueAxis.majorWidth.toDouble())
          currentPosition -= pxPerLine
        }
      }

      // Minor
      // TODO: Fix minor axis
      if (settings.fixedValueAxis.minorEvery > 1) {
        ctx.fill = settings.fixedValueAxis.minorColor.getColor()
        currentPosition = canvas.height
        val pxPerLine = canvas.height / (1.0 * largestData / settings.fixedValueAxis.minorEvery)
        if (!settings.fixedValueAxis.firstMinorIncluded) {
          currentPosition -= pxPerLine
        }
        while (currentPosition > 0) {
          ctx.fillRect(
              0.0, currentPosition, canvas.width, settings.fixedValueAxis.minorWidth.toDouble())
          currentPosition -= pxPerLine
        }
      }

      // Text
      if (settings.fixedValueAxis.textEvery > 1) {
        ctx.fill = settings.fixedValueAxis.textColor.getColor()
        currentPosition = canvas.height
        val pxPerLine = canvas.height / (1.0 * largestData / settings.fixedValueAxis.textEvery)
        var currentValue: Long = 0
        if (!settings.fixedValueAxis.firstTextIncluded) {
          currentPosition -= pxPerLine
          currentValue += settings.fixedValueAxis.textEvery
        }
        while (currentPosition > 0) {
          ctx.fillText(
              parseValueText(currentValue, settings.fixedValueAxis.textValue),
              settings.fixedValueAxis.textXOffset,
              currentPosition + settings.fixedValueAxis.textYOffset)
          currentValue += settings.fixedValueAxis.textEvery
          currentPosition -= pxPerLine
        }
      }
    }

    var hasInitialPoint = false
    if (settings.enableLine) {
      ctx.beginPath()
    }

    // Render, starting from tail to front
    for (i in data.indices) {
      // Creates coordinates for right-to-left history, and bottom-to-top values)
      val y =
          (canvas.height -
              (canvas.height - settings.dotSize) *
                  (1.0 * data[(i + dataPointer) % data.size] / largestData) -
              (settings.dotSize / 2).toDouble())
      val x =
          (-(pixelsPerMillisecond * msSinceLastPoint) +
              i * spaceBetweenPoints + spaceBetweenPoints) - settings.dotSize / 2

      // Dots
      if (settings.enableDots) {
        ctx.fill = settings.dotColor.getColor()
        ctx.fillOval(
            x - settings.dotSize / 2,
            y - settings.dotSize / 2,
            settings.dotSize.toDouble(),
            settings.dotSize.toDouble())
      }

      // lines
      if (settings.enableLine) {
        if (hasInitialPoint) {
          ctx.lineTo(x, y)
        } else {
          ctx.moveTo(x, y)
          hasInitialPoint = true
        }
      }

      // Bars
      if (settings.enableBars) {
        ctx.fill = settings.barColor.getColor()
        ctx.fillRect(
            x - settings.barWidth / 2,
            y,
            settings.barWidth.toDouble(),
            canvas.height - y - (settings.dotSize / 2).toDouble())
      }
    }

    if (settings.enableLine) {
      ctx.stroke = settings.lineColor.getColor()
      ctx.lineWidth = settings.lineWidth.toDouble()
      ctx.stroke()
    }
  }

  override fun shutdown() {
    stats.shutdown()
    super.shutdown()
  }

  override fun onSettingsChange(settings: GraphStatsSettings) {
    stats.bufferSize = settings.statsBufferSize

    this.settings = settings.copy()
    if (!settings.enableDots) {
      settings.dotSize = 0
    }

    data = LongArray(settings.graphPoints)
    msBetweenPoints = settings.graphHistoryTimeMS / settings.graphPoints

    scene.root.style = String.format(ROOT_STYLE, settings.backgroundColor.colorHex)

    updateCanvasSize()
  }

  private fun updateCanvasSize() {
    canvas.width = scene.width - settings.paddingLeft.toDouble() - settings.paddingRight.toDouble()
    canvas.height =
        scene.height - settings.paddingTop.toDouble() - settings.paddingBottom.toDouble()

    // Set padding
    AnchorPane.setTopAnchor(canvas, settings.paddingTop.toDouble())
    AnchorPane.setRightAnchor(canvas, settings.paddingRight.toDouble())
    AnchorPane.setBottomAnchor(canvas, settings.paddingTop.toDouble())
    AnchorPane.setLeftAnchor(canvas, settings.paddingLeft.toDouble())

    spaceBetweenPoints = (canvas.width + settings.dotSize) / data.size
    pixelsPerMillisecond = (canvas.width + settings.dotSize) / settings.graphHistoryTimeMS

    timeAxisPxBetweenMinor = 0.0
    if (settings.timeAxis.minorEvery > 1) {
      timeAxisPxBetweenMinor =
          canvas.width / (1.0 * settings.graphHistoryTimeMS / settings.timeAxis.minorEvery)
    }
    timeAxisPxBetweenMajor = 0.0
    if (settings.timeAxis.majorEvery > 1) {
      timeAxisPxBetweenMajor =
          canvas.width / (1.0 * settings.graphHistoryTimeMS / settings.timeAxis.majorEvery)
    }
    timeAxisPxBetweenText = 0.0
    if (settings.timeAxis.textEvery > 1) {
      timeAxisPxBetweenText =
          canvas.width / (1.0 * settings.graphHistoryTimeMS / settings.timeAxis.textEvery)
    }
  }

  private fun parseTimeText(ms: Long, value: String): String {
    return value.replace(
        GraphStatsSettings.TIME_AXIS_VALUE_S, Math.round(ms / 1000.0).toString() + "")
        .replace(GraphStatsSettings.TIME_AXIS_VALUE_MS, ms.toString() + "")
  }

  private fun parseValueText(value: Long, string: String): String {
    return string.replace(GraphStatsSettings.VALUE_AXIS_VALUE, value.toString() + "")
  }

  companion object {
    private val CANVAS_STYLE = "-fx-background-color: transparent;"
    private val ROOT_STYLE = "-fx-background-color: %s"
  }
}
