package me.fru1t.javafx

import javafx.scene.paint.Color

/** A serializable implementation of the JavaFX Color class allowing easy JSON support. */
class SerializableColor {
  // Non-serializable backing color
  @Transient private var color: Color? = null
  // Serialized color
  var colorHex = "#000"
    private set

  /** Creates a new serializable color from the given JavaFX Color. */
  constructor(color: Color) {
    setColor(color)
  }

  /** Creates a new serializable color from the given string web color. */
  constructor(color: String) {
    setColor(color)
  }

  /** Sets this color to the JavaFX [color]. */
  fun setColor(color: Color) {
    colorHex = FxUtils.colorToHex(color)
    this.color = color
  }

  /** Sets this color to the web [color]. */
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
