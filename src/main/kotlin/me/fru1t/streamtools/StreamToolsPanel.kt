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
    private val openSansFont = FontsRegister.openSans.deriveFont(14f)
    private val desktopHints: Map<*, *> =
      Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints") as Map<*, *>

    private val frameBackgroundColor = Color.WHITE

    private val textBarTextColor = Color.BLACK
    private const val textBarWidth = 100
    private const val textBarFps = "FPS: %d"
    private const val textBarApm = "APM: %d"
    private const val textBarHi = "Hi"

    private val historyGraphBarColor = Color.BLUE
    private const val HISTORY_GRAPH_BAR_APM_MIN_VALUE = 100
  }

  // Metrics
  private val metricsStore = MetricsStore()

  // Text
  private val textSideBarRenderer: TextSideBarRenderer by lazy {
    TextSideBarRenderer(x = size.width - textBarWidth, y = 14, lineHeight = 14)
  }

  // Graph
  private val historyGraphWidth = size.width - textBarWidth
  private val historyGraphBarWidth = historyGraphWidth / MetricsStore.HISTORICAL_APM_BUFFER_SIZE
  private var historyGraphApmMax = HISTORY_GRAPH_BAR_APM_MIN_VALUE
  private lateinit var historyGraphApmValues: ArrayList<Int>
  private var historyGraphBarHeightTempValue = 0

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
    g.font = openSansFont
    textSideBarRenderer.drawString(g, textBarHi)
    textSideBarRenderer.drawString(g, textBarApm.format(metricsStore.getInstantApm()))

    // Bar graph
    g.color = historyGraphBarColor
    historyGraphApmValues = metricsStore.getHistoricalApm()
    historyGraphApmMax = max(historyGraphApmValues.max(), HISTORY_GRAPH_BAR_APM_MIN_VALUE)
    historyGraphApmValues.forEachIndexed { i, value ->
      historyGraphBarHeightTempValue = (1.0 * value / historyGraphApmMax * size.height).toInt()
      g.fillRect(
        i * historyGraphBarWidth,
        size.height - historyGraphBarHeightTempValue,
        historyGraphBarWidth,
        historyGraphBarHeightTempValue
      )
    }
    g.color = textBarTextColor
    g.drawString(metricsStore.getHistoricalApm().toString(), 0, 100)

    // FPS
    currentRepaintTime = System.currentTimeMillis()
    lastRepaintTimeDelta[lastRepaintTimeDeltaIndex++] = currentRepaintTime - lastRepaintTime
    lastRepaintTimeDeltaIndex %= lastRepaintTimeDelta.size
    textSideBarRenderer.drawString(
      g,
      textBarFps.format((1000.0 * lastRepaintTimeDelta.size / lastRepaintTimeDelta.sum()).toInt())
    )
    lastRepaintTime = currentRepaintTime
  }

  override fun preferredSize(): Dimension = size
}
