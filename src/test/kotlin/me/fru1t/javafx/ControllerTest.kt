package me.fru1t.javafx

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ControllerTest {
  private lateinit var testController: Controller

  @Before
  fun setup() {
    testController = FakeController()
  }

  @Test
  fun getFxmlResourcePath() {
    assertThat(testController.getFxmlResourcePath()).isEqualTo("/FXML/test_fxml.fxml")
  }
}
