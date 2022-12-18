package me.fru1t.streamtools.widgets

import java.awt.Graphics2D

/** Methods for rendering the sidebar text. */
class TextSideBarRenderer(private val x: Int, private val y: Int, private val lineHeight: Int) {
  private var line = 0

  /** Resets the renderer at the start of a #paintComponent call. */
  fun reset() {
    line = 0
  }

  /** Renders the given [s] at the next available line on text side bar. */
  fun drawString(g: Graphics2D, s: String) {
    g.drawString(s, x, line++ * lineHeight + y)
  }
}