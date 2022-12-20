package me.fru1t.streamtools

import me.fru1t.streamtools.widgets.TextSideBarRenderer
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Toolkit
import javax.swing.JPanel
import kotlin.math.max

/** The viewing panel displaying the graph and metrics. */
class StreamToolsPanel(private val size: Dimension) : JPanel() {
  private companion object {
    private val textBarFont = FontsRegister.openSansBold.deriveFont(14f)
    private val historyGraphFont = FontsRegister.openSans.deriveFont(12f)
    private val desktopHints: Map<*, *> =
      Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints") as Map<*, *>

    private val frameBackgroundColor = Color.BLACK

    private val textBarTextColor = Color.WHITE
    private val textBarBackgroundColor = Color(100, 100, 200)
    private const val textBarWidth = 120
    private const val textBarFps = "FPS: %d"
    private const val textBarApm = "APM: %d"
    private const val textBarCpm = "CPM: %d"
    private const val textBarMpm = "MPM: %d"
    private const val textBarActions = "Actions: %d"
    private const val textBarClicks = "Clicks: %d"

    private val historyGraphBorderColor = Color(150, 150, 255)
    private val historyGraphBarColor = Color(200, 200, 255)
    private const val HISTORY_GRAPH_BAR_APM_MIN_VALUE = 50

    private const val HISTORY_GRAPH_GRID_LINES_EVERY = 25
    private val historyGraphGridLinesColor = Color(125, 125, 200)

    private const val SHOW_FPS = false
  }

  // Metrics
  private val metricsStore = MetricsStore()

  // Graph
  private val historyGraphBarWidth = (size.width - textBarWidth) / MetricsStore.HISTORICAL_APM_BUFFER_SIZE
  private val historyGraphWidth = historyGraphBarWidth * MetricsStore.HISTORICAL_APM_BUFFER_SIZE
  private var historyGraphApmMax = HISTORY_GRAPH_BAR_APM_MIN_VALUE
  private lateinit var historyGraphApmValues: ArrayList<Int>
  private var historyGraphBarHeightTempValue = 0
  private var historyGraphGridlinesCounter = 0
  private var historyGraphGridLinesHeightTempValue = 0

  // Text
  private val textSideBarRenderer: TextSideBarRenderer by lazy {
    TextSideBarRenderer(
      x = historyGraphWidth + 2,
      y = 14,
      width = textBarWidth,
      lineHeight = 14,
      textColor = textBarTextColor,
      textFont = textBarFont,
      backgroundColor = textBarBackgroundColor,
      textPadding = 2
    )
  }

  // FPS
  private var lastRepaintTime = System.currentTimeMillis()
  private var currentRepaintTime = System.currentTimeMillis()
  private val lastRepaintTimeDelta = LongArray(10)
  private var lastRepaintTimeDeltaIndex = 0

  override fun paintComponent(g: Graphics?) {
    if (g == null) {
      return
    }
    g as Graphics2D

    // Clear frame
    g.color = frameBackgroundColor
    g.fillRect(0, 0, size.width, size.height)
    textSideBarRenderer.reset()

    // Fix text aliasing (https://stackoverflow.com/questions/31536952/how-to-fix-text-quality-in-java-graphics)
    g.setRenderingHints(desktopHints)

    // Text
    g.color = textBarTextColor
    g.font = textBarFont
    textSideBarRenderer.addString(textBarApm.format(metricsStore.getInstantApm()))
    textSideBarRenderer.addString(textBarCpm.format(metricsStore.getInstantClicksPerMinute()))
    textSideBarRenderer.addString(textBarMpm.format(metricsStore.getInstantMovementPerMinute()))
    textSideBarRenderer.addString(textBarActions.format(metricsStore.getTotalActions()))
    textSideBarRenderer.addString(textBarClicks.format(metricsStore.getTotalMouseClicks()))

    // Bar graph
    g.color = historyGraphBorderColor
    g.drawRect(0, 0, historyGraphWidth, size.height - 1)

    historyGraphApmValues = metricsStore.getHistoricalActions()
    historyGraphApmMax = max(historyGraphApmValues.max(), HISTORY_GRAPH_BAR_APM_MIN_VALUE)

    historyGraphGridlinesCounter = HISTORY_GRAPH_GRID_LINES_EVERY
    g.color = historyGraphGridLinesColor
    g.font = historyGraphFont
    for (historyGraphGridlinesCounter in HISTORY_GRAPH_GRID_LINES_EVERY until historyGraphApmMax step HISTORY_GRAPH_GRID_LINES_EVERY) {
      historyGraphGridLinesHeightTempValue =
        size.height - (1.0 * historyGraphGridlinesCounter / historyGraphApmMax * (size.height - 1)).toInt()
      g.drawLine(1, historyGraphGridLinesHeightTempValue, historyGraphWidth - 1, historyGraphGridLinesHeightTempValue)
      g.drawString(historyGraphGridlinesCounter.toString(), 2, historyGraphGridLinesHeightTempValue - 2)
    }

    g.color = historyGraphBarColor
    historyGraphApmValues.forEachIndexed { i, value ->
      historyGraphBarHeightTempValue = (1.0 * value / historyGraphApmMax * (size.height - 1)).toInt()
      g.fillRect(
        i * historyGraphBarWidth,
        size.height - historyGraphBarHeightTempValue - 1,
        historyGraphBarWidth,
        historyGraphBarHeightTempValue
      )
    }

    // FPS
    if (SHOW_FPS) {
      g.font = textBarFont
      g.color = textBarTextColor
      currentRepaintTime = System.currentTimeMillis()
      lastRepaintTimeDelta[lastRepaintTimeDeltaIndex++] = currentRepaintTime - lastRepaintTime
      lastRepaintTimeDeltaIndex %= lastRepaintTimeDelta.size
      textSideBarRenderer.addString(
        textBarFps.format((1000.0 * lastRepaintTimeDelta.size / lastRepaintTimeDelta.sum()).toInt())
      )
      lastRepaintTime = currentRepaintTime
    }

    textSideBarRenderer.paintComponent(g)
  }

  override fun preferredSize(): Dimension = size
}
