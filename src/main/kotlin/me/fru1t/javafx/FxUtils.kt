package me.fru1t.javafx

import javafx.scene.Node
import javafx.scene.control.CheckBox
import javafx.scene.control.Slider
import javafx.scene.control.TextField
import javafx.scene.paint.Color

/** Util methods for unit conversion or node binding. */
object FxUtils {

  /** Denotes how to show the value from the slider into the TextField. */
  enum class SliderTextFieldType {
    /** Shows whole numbers (integers) within the TextField (eg. 1, 4, 7, ...). */
    INTEGER,
    /** Shows decimal numbers (doubles) within the TextField (eg. 1.5, 3.14, 20.40, ...). */
    DECIMAL
  }

  /** Denotes the type of value a TextView can contain. */
  enum class TextFieldType(val defaultValue: String) {
    /** The value of the TextField may only be whole numbers (eg. 1, 4, 7, ...). */
    INTEGER("0"),
    /** The value of the TextField may only be decimal numbers (eg. 1.5, 3.14, 20.40, ...). */
    DECIMAL("0")
  }

  /** Converts a JavaFX [color] to a css-friendly Hex string in the format of "#RRGGBB". */
  fun colorToHex(color: Color): String {
    return String.format(
        "#%02X%02X%02X",
        (color.red * 255).toInt(),
        (color.green * 255).toInt(),
        (color.blue * 255).toInt())
  }

  /**
   * Binds a [slider] and a [textField] such that when either is changed, the values are updated
   * in both views. If the user enters invalid input in the [textField], no changes will occur.
   */
  fun bindSliderToTextField(slider: Slider, textField: TextField, type: SliderTextFieldType) {
    slider.valueProperty().addListener { _, _, newValue ->
      // If the textField is in focus, it means the textField is changing the slider value,
      // so we don't want to cause a feedback loop.
      if (!textField.isFocused) {
        textField.text = when (type) {
          FxUtils.SliderTextFieldType.INTEGER -> newValue.toInt().toString()
          FxUtils.SliderTextFieldType.DECIMAL -> newValue.toString()
        }
      }
    }
    textField.textProperty().addListener { _, oldValue, newValue ->
      if (!slider.isFocused) {
        try {
          slider.value = newValue.toDouble()
        } catch (e: NumberFormatException) {
          textField.text = oldValue
        }
      }
    }
  }

  /**
   * Binds [textFields] to a [type] such that the [textFields] will enforce that their values are
   * a [type] after user input. The [textFields] will revert to their previous, valid state if
   * invalid input is entered.
   */
  fun bindTextFieldToType(type: TextFieldType, vararg textFields: TextField) {
    for (textField in textFields) {
      textField.textProperty().addListener { _, _, newValue ->
        try {
          when (type) {
            FxUtils.TextFieldType.INTEGER -> newValue.toInt()
            FxUtils.TextFieldType.DECIMAL -> newValue.toDouble()
          }
        } catch (e: NumberFormatException) {
          textField.text = type.defaultValue
        }
      }
    }
  }

  /**
   * Binds a [checkBox] to enable or disable [nodes] (via their `isDisable` property). When
   * [enableOnSelected] is `true`, the [nodes] will be enabled only when the [checkBox] is checked.
   * When [enableOnSelected] is `false`, the [nodes] will be enabled only when the [checkBox] is
   * not checked.
   */
  fun bindCheckBoxToDisableNode(checkBox: CheckBox, enableOnSelected: Boolean, vararg nodes: Node) {
    checkBox.selectedProperty().addListener { _, _, newValue ->
      for (node in nodes) {
        // The logic table of this function is an xnor where true-true or false-false
        // results in false (don't disable), whereas true-false or false-true results in
        // true (to disable).
        node.isDisable = enableOnSelected != newValue
      }
    }
  }
}
