package me.fru1t.streamtools

import org.lwjgl.opengl.GL11

/** Rendering utility methods for OpenGL. */
class GlRenderer(private val windowWidth: Int, private val windowHeight: Int) {
  private val drawRectPixelToFloatTranslation: FloatArray = FloatArray(4)

  /** Draws a rectangle using the coordinate system (x, y) starting at the bottom left, in pixels. */
  fun drawRect(x: Int, y: Int, width: Int, height: Int, color: FloatArray) {
    drawRectPixelToFloatTranslation[0] = 2.0f * x / windowWidth - 1.0f
    drawRectPixelToFloatTranslation[1] = 2.0f * y / windowHeight - 1.0f
    drawRectPixelToFloatTranslation[2] = 2.0f * width / windowWidth - 1.0f
    drawRectPixelToFloatTranslation[3] = 2.0f * height / windowHeight - 1.0f

    GL11.glColor4fv(color)
    GL11.glBegin(GL11.GL_QUADS)
    GL11.glVertex2f(drawRectPixelToFloatTranslation[0], drawRectPixelToFloatTranslation[1])
    GL11.glVertex2f(drawRectPixelToFloatTranslation[2], drawRectPixelToFloatTranslation[1])
    GL11.glVertex2f(drawRectPixelToFloatTranslation[2], drawRectPixelToFloatTranslation[3])
    GL11.glVertex2f(drawRectPixelToFloatTranslation[0], drawRectPixelToFloatTranslation[3])
    GL11.glEnd()
  }
}