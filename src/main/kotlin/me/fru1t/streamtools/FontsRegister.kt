package me.fru1t.streamtools

import java.awt.Font
import java.awt.GraphicsEnvironment

/** Holds references to bundled fonts. */
object FontsRegister {
  val openSans: Font by lazyFont("OpenSans-Regular.ttf")
  val openSansBold: Font by lazyFont("OpenSans-Bold.ttf")

  private fun lazyFont(filePath: String): Lazy<Font> {
    return lazy {
      val font = Font.createFont(Font.TRUETYPE_FONT, ClassLoader.getSystemResourceAsStream(filePath))
      GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font)
      font
    }
  }
}