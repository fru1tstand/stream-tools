package me.fru1t.javafx

import javafx.scene.Scene
import javafx.stage.Stage

/** An empty implementation of Controller for testing. */
@FxmlResource("/FXML/Valid.fxml")
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
