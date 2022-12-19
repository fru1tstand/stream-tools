package me.fru1t.streamtools

import com.github.kwhat.jnativehook.GlobalScreen
import java.awt.Dimension
import java.util.concurrent.CountDownLatch
import javax.swing.JFrame
import javax.swing.SwingUtilities
import javax.swing.WindowConstants

fun main() {
  GlobalScreen.registerNativeHook()

  val application = StreamToolsApplication()
  val countDownLatch = CountDownLatch(1)
  SwingUtilities.invokeLater {
    application.createAndShow()
    countDownLatch.countDown()
  }
  countDownLatch.await()
  application.loop()
}

/**
 * The window manager for StreamTools which handles bootstrapping the application, frame constructions, and showing the
 * application window.
 */
class StreamToolsApplication {
  companion object {
    private val WINDOW_SIZE = Dimension(500, 300)

    private const val RENDERER_FPS = 144
    private const val _RENDERER_FPS_SLEEP_MS: Long = 1000L / RENDERER_FPS
  }

  private val frame: JFrame = JFrame("StreamToolsApplication")
  private val streamToolsPanel = StreamToolsPanel(WINDOW_SIZE)

  private var lastFrameRenderTime: Long = 0
  private var currentFrameRenderTime: Long = 0
  private var fpsSleepDeltaMs: Long = 0
  private var lastFpsPrintTime: Long = 0

  fun createAndShow() {
    frame.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
    frame.isResizable = false
    frame.contentPane.add(streamToolsPanel)
    frame.pack()
    frame.isVisible = true
    streamToolsPanel.isDoubleBuffered = true
  }

  fun loop() {
    lastFrameRenderTime = System.currentTimeMillis()
    currentFrameRenderTime = lastFrameRenderTime
    lastFpsPrintTime = lastFrameRenderTime
    fpsSleepDeltaMs = _RENDERER_FPS_SLEEP_MS


    while (frame.isVisible) {
      streamToolsPanel.repaint()

      // Calculate the remaining sleep time to keep the FPS at the set rendering fps
      currentFrameRenderTime = System.currentTimeMillis()
      fpsSleepDeltaMs = _RENDERER_FPS_SLEEP_MS - (currentFrameRenderTime - lastFrameRenderTime)
      lastFrameRenderTime = currentFrameRenderTime
      if (fpsSleepDeltaMs > 0) {
        Thread.sleep(fpsSleepDeltaMs)
      }
    }
    println("Window closed, stopping loop")
  }
}
