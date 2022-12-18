package me.fru1t.streamtools

import java.awt.Dimension
import java.awt.Graphics
import javax.swing.JPanel

class StreamToolsPanel(private val size: Dimension) : JPanel() {
  private companion object {
  }

  private var lastRepaintTime = System.currentTimeMillis()
  private var currentRepaintTime = System.currentTimeMillis()
  private val lastRepaintTimeDelta = LongArray(10)
  private var lastRepaintTimeDeltaIndex = 0

  override fun paintComponent(g: Graphics?) {
    super.paintComponent(g)
    if (g == null) {
      return
    }

    // Text
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