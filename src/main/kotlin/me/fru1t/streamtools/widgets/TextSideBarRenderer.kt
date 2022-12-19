package me.fru1t.streamtools.widgets

import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D

/** Methods for rendering the sidebar text. */
class TextSideBarRenderer(
  private val x: Int,
  private val y: Int,
  private val width: Int,
  private val lineHeight: Int,
  private val textColor: Color,
  private val textFont: Font,
  private val backgroundColor: Color,
  private val textPadding: Int
) {
  private var line = 0
  private val content = mutableListOf<String>()

  /** Resets the renderer at the start of a #paintComponent call. */
  fun reset() {
    line = 0
    content.clear()
  }

  /** Queues up a string to be rendered. */
  fun addString(s: String) {
    content.add(s)
  }

  fun paintComponent(g: Graphics2D) {
    g.color = backgroundColor
    g.fillRect(x, 0, width, content.size * lineHeight + 4)
    g.color = textColor
    g.font = textFont
    content.forEachIndexed { i, s -> g.drawString(s, x + textPadding, i * lineHeight + y) }
  }
}