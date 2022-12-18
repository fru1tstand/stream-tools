package me.fru1t.streamtools

import me.fru1t.streamtools.widgets.TextSideBarRenderer
import java.awt.Color
import java.awt.Dimension
import java.awt.FontMetrics
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Toolkit
import javax.swing.JPanel

/** The viewing panel displaying the graph and metrics. */
class StreamToolsPanel(private val size: Dimension) : JPanel() {
  private companion object {
    private val openSansFont = FontsRegister.openSans.deriveFont(14f)
    private val desktopHints: Map<*, *> =
      Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints") as Map<*, *>

    private val frameBackgroundColor = Color.GREEN

    private val textBarTextColor = Color.BLACK
    private const val textBarWidth = 75
    private const val textBarFps = "FPS: %d"
    private const val textBarHi = "Hi"
  }

  private lateinit var g2d: Graphics2D

  // Text
  private val textSideBarRenderer: TextSideBarRenderer by lazy {
    TextSideBarRenderer(x = size.width - textBarWidth, y = 14, lineHeight = 14)
  }

  // FPS
  private var lastRepaintTime = System.currentTimeMillis()
  private var currentRepaintTime = System.currentTimeMillis()
  private val lastRepaintTimeDelta = LongArray(10)
  private var lastRepaintTimeDeltaIndex = 0

  // Derived constants
  private val _textBarX = size.width - textBarWidth

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

    // Bar graph

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