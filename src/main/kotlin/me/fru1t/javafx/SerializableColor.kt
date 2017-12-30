package me.fru1t.javafx

import javafx.scene.paint.Color

/** Represents a serializable version of the JavaFX Color class.  */
class SerializableColor {
  @Transient private var color: Color? = null
  var colorHex = "#000"
    private set

  /**
   * Creates a new serializable color from the given JavaFX Color.
   */
  constructor(color: Color) {
    setColor(color)
  }

  /**
   * Creates a new serializable color from the given string web color.
   */
  constructor(color: String) {
    setColor(color)
  }

  fun setColor(color: Color) {
    colorHex = FXUtils.colorToHex(color)
    this.color = color
  }

  /**
   * @param color A web representation of a color.
   * @see Color.web
   */
  fun setColor(color: String) {
    colorHex = color
    this.color = Color.web(color)
  }

  fun getColor(): Color {
    if (color == null) {
      color = Color.web(colorHex)
    }
    return color!!
  }

  override fun toString(): String {
    return colorHex
  }
}
