package me.fru1t.streamtools

import com.google.common.truth.Truth.assertThat
import javafx.stage.Stage
import me.fru1t.javafx.test.FxApplicationTest
import me.fru1t.javafx.test.FxTest

class StreamToolsApplicationTest : FxApplicationTest() {
  @FxTest
  fun start() {
    val streamToolsApplication = StreamToolsApplication()
    val stage = Stage()
    streamToolsApplication.start(stage)
    assertThat(stage.isShowing).isTrue()
  }
}
