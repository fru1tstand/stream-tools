package me.fru1t.javafx.test

import com.nhaarman.mockito_kotlin.validateMockitoUsage
import javafx.application.Platform
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.testfx.framework.junit5.ApplicationTest
import java.lang.reflect.Method
import java.util.concurrent.CountDownLatch
import java.util.stream.Stream

/**
 * Base test runner class that allows the use of the JavaFX thread. Tests that require the JavaFX
 * thread (eg. tests that require the use of a Stage) must be annotated with [FxTest]. Note that
 * these are NOT thread safe tests meaning each [FxTest] should instantiate its own test objects.
 * All [FxTest] annotated tests will be run under the [fxTests] [TestFactory]. Any other tests that
 * don't require JavaFX may be annotated normally with [Test].
 *
 * An example use of this class is as follows:
 * ```
 * class MyClassTest : FxApplicationTest() {
 *   private lateinit var myClass: MyClass
 *
 *   @BeforeEach
 *   fun setup() {
 *     myClass = MyClass()
 *   }
 *
 *   @Test
 *   fun methodA() {
 *     myClass.doSomethingThatDoesNotNeedJavaFx()
 *     assertThat(myClass.didSucceed()).isTrue()
 *   }
 *
 *   @FxTest
 *   fun methodB() {
 *     val stage = Stage()
 *     myClass.doSomethingWithStage(stage);
 *     assertThat(stage.isShowing).isTrue()
 *   }
 * }
 * ```
 *
 */
abstract class FxApplicationTest : ApplicationTest() {
  @Suppress("MemberVisibilityCanPrivate")
  @TestFactory
  fun fxTests(): Stream<DynamicTest> = javaClass.methods
        .filter { it.getAnnotation(FxTest::class.java) != null }
        .stream()
        .map { dynamicTest("${it.name}()", { synchronouslyRunTestOnFxThread(it) }) }

  private fun synchronouslyRunTestOnFxThread(method: Method) {
    val cdl = CountDownLatch(1)
    Platform.runLater {
      try {
        method.invoke(this)
        validateMockitoUsage()
      } finally {
        cdl.countDown()
      }
    }
    cdl.await()
  }
}
