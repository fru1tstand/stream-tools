package me.fru1t.streamtools

import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Toolkit
import javax.swing.JPanel

/** The viewing panel displaying the graph and metrics. */
class StreamToolsPanel(private val size: Dimension) : JPanel() {
  private companion object {
    private val openSansFont = FontsRegister.openSans.deriveFont(20f)
    private val desktopHints: Map<*, *> =
      Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints") as Map<*, *>
  }

  private var lastRepaintTime = System.currentTimeMillis()
  private var currentRepaintTime = System.currentTimeMillis()
  private val lastRepaintTimeDelta = LongArray(10)
  private var lastRepaintTimeDeltaIndex = 0

  private lateinit var g2d: Graphics2D

  override fun paintComponent(g: Graphics?) {
    super.paintComponent(g)
    if (g == null) {
      return
    }

    // Fix text aliasing (https://stackoverflow.com/questions/31536952/how-to-fix-text-quality-in-java-graphics)
    g2d = g as Graphics2D
    g2d.setRenderingHints(desktopHints)

    // Text
    g.font = openSansFont
    g.drawString("hi", 10, 20)

    // Bar graph

    // FPS
    currentRepaintTime = System.currentTimeMillis()
    lastRepaintTimeDelta[lastRepaintTimeDeltaIndex++] = currentRepaintTime - lastRepaintTime
    lastRepaintTimeDeltaIndex %= lastRepaintTimeDelta.size
    g.drawString((1000.0 * lastRepaintTimeDelta.size / lastRepaintTimeDelta.sum()).toInt().toString(), 30, 30)
    lastRepaintTime = currentRepaintTime
  }

  override fun preferredSize(): Dimension = size
}