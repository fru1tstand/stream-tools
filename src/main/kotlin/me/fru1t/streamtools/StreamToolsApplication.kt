package me.fru1t.streamtools

import org.lwjgl.Version
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFW.GLFW_FALSE
import org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE
import org.lwjgl.glfw.GLFW.GLFW_RELEASE
import org.lwjgl.glfw.GLFW.GLFW_RESIZABLE
import org.lwjgl.glfw.GLFW.GLFW_VISIBLE
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.glfw.GLFWVidMode
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT
import org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil.NULL
import java.nio.IntBuffer

fun main() {
  println("LWJGL Version: ${Version.getVersion()}")

  val application = StreamToolsApplication()
  application.loop()
}

class StreamToolsApplication {
  companion object {
    private const val WINDOW_HEIGHT = 300
    private const val WINDOW_WIDTH = 500

    private const val RENDERER_FPS = 350
    private const val _RENDERER_FPS_SLEEP_MS: Long = 1000L / RENDERER_FPS

    private val BAR_GRAPH_BAR_COLOR: FloatArray = floatArrayOf(0.0f, 0.0f, 1.0f, 1.0f)
  }

  private val window: Long
  private val glRenderer: GlRenderer = GlRenderer(WINDOW_WIDTH, WINDOW_HEIGHT)

  private var lastFrameRenderTime: Long = 0
  private var currentFrameRenderTime: Long = 0
  private var fpsSleepDeltaMs: Long = 0
  private var fps: Int = 0
  private var lastFpsPrintTime: Long = 0

  init {
    GLFWErrorCallback.createPrint(System.err).set()

    // Initialize GLFW
    if (!GLFW.glfwInit()) {
      throw IllegalStateException("Unable to initialize GLFW")
    }

    // Set up rendering window
    GLFW.glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE)
    GLFW.glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE)
    window = GLFW.glfwCreateWindow(WINDOW_WIDTH, WINDOW_HEIGHT, "StreamTools", NULL, NULL)
    if (window == NULL) {
      throw RuntimeException("Could not create GLFW window")
    }

    // Set up interrupt callback
    GLFW.glfwSetKeyCallback(window) { win, key, _, action, _ ->
      if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
        GLFW.glfwSetWindowShouldClose(win, true)
      }
    }

    // Push a new frame to the stack
    MemoryStack.stackPush().use {
      // Grab window size
      val pWidth: IntBuffer = it.mallocInt(1)
      val pHeight: IntBuffer = it.mallocInt(1)
      GLFW.glfwGetWindowSize(window, pWidth, pHeight)

      // Get monitor window
      val videoMode: GLFWVidMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor())!!

      // Center window to monitor
      GLFW.glfwSetWindowPos(window, (videoMode.width() - pWidth.get(0)) / 2, (videoMode.height() - pHeight.get(0)) / 2)
    }

    GLFW.glfwMakeContextCurrent(window)
    GLFW.glfwShowWindow(window)
    GLFW.glfwSwapInterval(0)

    GL.createCapabilities()
    GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
  }

  fun loop() {
    lastFrameRenderTime = System.currentTimeMillis()
    currentFrameRenderTime = lastFrameRenderTime
    lastFpsPrintTime = lastFrameRenderTime
    fpsSleepDeltaMs = _RENDERER_FPS_SLEEP_MS

    while (!GLFW.glfwWindowShouldClose(window)) {
      // Poll for escape events
      GLFW.glfwPollEvents()

      // Redraw frame
      GL11.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
      redraw()
      GLFW.glfwSwapBuffers(window)

      // Calculate the remaining sleep time to keep the FPS at the set rendering fps
      currentFrameRenderTime = System.currentTimeMillis()
      fpsSleepDeltaMs = _RENDERER_FPS_SLEEP_MS - (currentFrameRenderTime - lastFrameRenderTime)
      lastFrameRenderTime = currentFrameRenderTime

      // Print FPS to console
      ++fps
      if (currentFrameRenderTime - lastFpsPrintTime >= 1000) {
        println(fps)
        fps = 0
        lastFpsPrintTime = currentFrameRenderTime
      }

      if (fpsSleepDeltaMs > 0) {
        Thread.sleep(fpsSleepDeltaMs)
      }
    }
  }

  private fun redraw() {
    glRenderer.drawRect(0, 0, 100, 200, BAR_GRAPH_BAR_COLOR)
  }
}


