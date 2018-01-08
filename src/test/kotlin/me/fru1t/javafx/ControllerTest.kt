package me.fru1t.javafx

import com.google.common.truth.Truth.assertThat
import javafx.scene.Scene
import javafx.scene.layout.Pane
import javafx.stage.Stage
import me.fru1t.javafx.test.FxApplicationTest
import me.fru1t.javafx.test.FxTest
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class ControllerTest : FxApplicationTest() {

  @Test
  fun getFxmlResourcePath() {
    val testController: Controller = ValidController()
    assertThat(testController.getFxmlResourcePath()).isEqualTo("/FXML/ControllerTest_Valid.fxml")
  }

  @FxTest
  fun show() {
    val stage = Stage()
    val scene = Scene(Pane())
    val testController: Controller = ValidController(scene, stage)
    testController.show()

    assertThat(stage.scene).isEqualTo(scene)
    assertThat(stage.isShowing).isTrue()
  }

  @Test
  fun show_noStage() {
    val testController = ValidController(null, null)
    testController.show()
    assertThat(testController.stage).isNull()
  }

  @FxTest
  fun hide() {
    val stage = Stage()
    val scene = Scene(Pane())
    stage.show()
    val testController: Controller = ValidController(scene, stage)
    testController.hide()

    assertThat(stage.isShowing).isFalse()
  }

  @Test
  fun hide_noStage() {
    val testController = ValidController(null, null)
    testController.hide()
    assertThat(testController.stage).isNull()
  }

  @FxTest
  fun shutdown() {
    val stage = Stage()
    val scene = Scene(Pane())
    stage.show()
    val testController: Controller = ValidController(scene, stage)
    testController.shutdown()

    assertThat(stage.isShowing).isFalse()
  }

  @FxTest
  fun onUpdate() {
    val stage = Stage()
    val scene = Scene(Pane())
    val testController: Controller = ValidController(scene, stage)
    testController.onUpdate(1)

    assertThat((testController as ValidController).onUpdateCalls).isEqualTo(1)
  }

  @Test
  fun create() {
    val testController = Controller.create(ValidController::class.java)
    assertThat(testController.scene.root).isInstanceOf(Pane::class.java)
    assertThat(testController.stage).isNull()
    assertThat(testController.onSceneCreateCalls).isEqualTo(1)
  }

  @Test
  fun create_noFxController() {
    assertThrows(
        RuntimeException::class.java, { Controller.create(NoControllerController::class.java) } )
  }

  @Test
  fun create_invalidResourcePath() {
    assertThrows(
        RuntimeException::class.java, { Controller.create(InvalidFxmlFileController::class.java)})
  }

  @Test
  fun create_noFxmlResourceAnnotation() {
    assertThrows(
        RuntimeException::class.java,
        { Controller.create(NoFxmlResourceAnnotationController::class.java) } )
  }

  @FxTest
  fun createWithNewStage() {
    val testController = Controller.createWithNewStage(ValidController::class.java)
    assertThat(testController.scene.root).isInstanceOf(Pane::class.java)
    assertThat(testController.stage).isNotNull()
    assertThat(testController.onSceneCreateCalls).isEqualTo(1)
  }
}

@FxmlResource("/FXML/ControllerTest_Valid.fxml")
class ValidController(scene: Scene? = null, stage: Stage? = null) : Controller() {
  var onSceneCreateCalls = 0
  var onUpdateCalls = 0

  init {
    this.stage = stage
    if (scene != null) {
      this.scene = scene
    }
  }

  override fun onSceneCreate() {
    super.onSceneCreate()
    ++onSceneCreateCalls
  }

  override fun onUpdate(now: Long) {
    super.onUpdate(now)
    ++onUpdateCalls
  }
}

class NoFxmlResourceAnnotationController : Controller()

@FxmlResource("/FXML/ControllerTest_NoController.fxml")
class NoControllerController : Controller()

@FxmlResource("/invalid/xml/path.fxml")
class InvalidFxmlFileController : Controller()
