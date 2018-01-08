package me.fru1t.javafx

import com.google.common.truth.Truth.assertThat
import javafx.beans.value.ChangeListener
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.control.CheckBox
import javafx.scene.control.Slider
import javafx.scene.control.TextField
import javafx.scene.paint.Color
import javafx.stage.Stage
import me.fru1t.javafx.test.FxApplicationTest
import me.fru1t.javafx.test.FxTest
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class FxUtilsTest : FxApplicationTest() {
  @Test
  fun colorToHex() {
    assertThat(FxUtils.colorToHex(Color(1.0, 0.0, 0.0, 1.0))).isEqualTo("#FF0000")
    assertThat(FxUtils.colorToHex(Color(0.0, 1.0, 0.0, 1.0))).isEqualTo("#00FF00")
    assertThat(FxUtils.colorToHex(Color(0.0, 0.0, 1.0, 1.0))).isEqualTo("#0000FF")
    assertThat(FxUtils.colorToHex(Color(1.0, 1.0, 1.0, 1.0))).isEqualTo("#FFFFFF")
    assertThat(FxUtils.colorToHex(Color(0.0, 0.0, 0.0, 1.0))).isEqualTo("#000000")
  }

  @FxTest
  fun fxUtilsTestController_selfTest() {
    val controller = inflateTestLayoutOnStageAndShow()

    // Slider focus
    controller.requestSliderFocus()
    assertThat(controller.slider.isFocused).isTrue()
    assertThat(controller.textField.isFocused).isFalse()

    // Text field focus
    controller.requestTextFieldFocus()
    assertThat(controller.slider.isFocused).isFalse()
    assertThat(controller.textField.isFocused).isTrue()
  }

  @FxTest
  fun bindSliderToTextField_int_sliderHasFocus() {
    val controller = inflateTestLayoutOnStageAndShow()
    FxUtils.bindSliderToTextField(
        controller.slider, controller.textField, FxUtils.SliderTextFieldType.INTEGER)
    controller.requestSliderFocus()
    controller.slider.value = 2.2

    assertThat(controller.textField.text).isEqualTo("2")

    // Verifies the textField change doesn't cause a feedback loop
    assertThat(controller.slider.value).isEqualTo(2.2)
  }

  @FxTest
  fun bindSliderToTextField_int_textFieldHasFocus() {
    val controller = inflateTestLayoutOnStageAndShow()
    FxUtils.bindSliderToTextField(
        controller.slider, controller.textField, FxUtils.SliderTextFieldType.INTEGER)
    controller.requestTextFieldFocus()
    controller.textField.text = "3"

    assertThat(controller.slider.value).isEqualTo(3.0)
  }

  @FxTest
  fun bindSliderToTextField_decimal_sliderHasFocus() {
    val controller = inflateTestLayoutOnStageAndShow()
    FxUtils.bindSliderToTextField(
        controller.slider, controller.textField, FxUtils.SliderTextFieldType.DECIMAL)
    controller.requestSliderFocus()
    controller.slider.value = 2.2

    assertThat(controller.textField.text).isEqualTo("2.2")
  }

  @FxTest
  fun bindSliderToTextField_decimal_textFieldHasFocus() {
    val controller = inflateTestLayoutOnStageAndShow()
    FxUtils.bindSliderToTextField(
        controller.slider, controller.textField, FxUtils.SliderTextFieldType.DECIMAL)
    controller.requestTextFieldFocus()
    controller.textField.text = "3.5"

    assertThat(controller.slider.value).isEqualTo(3.5)
  }

  @FxTest
  fun bindSliderToTextField_invalidTextFieldInput() {
    val controller = inflateTestLayoutOnStageAndShow()
    FxUtils.bindSliderToTextField(
        controller.slider, controller.textField, FxUtils.SliderTextFieldType.INTEGER)
    controller.requestTextFieldFocus()
    controller.textField.text = "2"
    controller.textField.text = "not a number"

    assertThat(controller.slider.value).isEqualTo(2.0)
    assertThat(controller.textField.text).isEqualTo("2")
  }

  @Test
  fun bindTextFieldToType_int() {
    val textField = TextField()
    FxUtils.bindTextFieldToType(FxUtils.TextFieldType.INTEGER, textField)
    textField.text = "4"

    assertThat(textField.text).isEqualTo("4")
  }

  @Test
  fun bindTextFieldToType_decimal() {
    val textField = TextField()
    FxUtils.bindTextFieldToType(FxUtils.TextFieldType.DECIMAL, textField)
    textField.text = "4.5"

    assertThat(textField.text).isEqualTo("4.5")
  }

  @Test
  fun bindTextFieldToType_invalid() {
    val textField = TextField()
    FxUtils.bindTextFieldToType(FxUtils.TextFieldType.INTEGER, textField)
    textField.text = "4.4"

    assertThat(textField.text).isEqualTo(FxUtils.TextFieldType.INTEGER.defaultValue)
  }

  @Test
  fun bindCheckBoxToDisableNode() {
    val checkBox = CheckBox()
    val enableOnCheckBoxEnabledNode: Node = TextField()
    val disableOnCheckBoxDisabledNode: Node = TextField()
    FxUtils.bindCheckBoxToDisableNode(checkBox, true, enableOnCheckBoxEnabledNode)
    FxUtils.bindCheckBoxToDisableNode(checkBox, false, disableOnCheckBoxDisabledNode)
    checkBox.isSelected = true

    assertThat(enableOnCheckBoxEnabledNode.isDisable).isFalse()
    assertThat(disableOnCheckBoxDisabledNode.isDisable).isTrue()
  }

  private fun inflateTestLayoutOnStageAndShow(): FxUtilsTestController {
    val loader = FXMLLoader(FxUtilsTest::class.java.getResource("/FXML/FxUtilsTest.fxml"))
    val stage = Stage()
    stage.scene = Scene(loader.load())
    val controller = loader.getController<FxUtilsTestController>()
    controller.stage = stage
    stage.show()
    return controller
  }
}

class FxUtilsTestController {
  @FXML lateinit var textField: TextField
  @FXML lateinit var slider: Slider
  lateinit var stage: Stage

  fun requestTextFieldFocus() {
    requestFocus(textField)
  }

  fun requestSliderFocus() {
    requestFocus(slider)
  }

  private fun requestFocus(node: Node) {
    if (!stage.isShowing) {
      fail<Unit>("In order to set focus an element, the element must be in a scene attached to " +
          "a stage that is showing.")
    }

    val cdl = CountDownLatch(1)
    val focusListener = ChangeListener<Boolean> { _, _, _ -> cdl.countDown() }
    node.focusedProperty().addListener(focusListener)
    node.requestFocus()
    if (!cdl.await(5, TimeUnit.SECONDS)) {
      fail<Unit>("Couldn't gain focus on ${node.id} within 5 seconds.")
    }
  }
}
