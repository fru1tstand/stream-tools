package me.fru1t.streamtools.util

import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import org.jnativehook.GlobalScreen
import org.jnativehook.NativeHookException
import org.jnativehook.keyboard.NativeKeyEvent
import org.jnativehook.keyboard.NativeKeyListener
import org.jnativehook.mouse.NativeMouseEvent
import org.jnativehook.mouse.NativeMouseMotionListener

import java.awt.*
import java.util.Date
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Tracks mouse and keyboard movements. Provides an interface to hook into receiving updates to
 * these values.
 *
 * @constructor Creates a new KeyboardAndMouseStatistics object with [bufferSize] for
 * calculating averages. One buffer slot is used every [getCurrentData] call which should be every
 * animation timer tick. The tick rate is different for each machine and should match the user's
 * monitor's refresh rate. See [KeyboardAndMouseStatistics.bufferSize]
 */
class KeyboardAndMouseStatistics constructor(bufferSize: Int = DEFAULT_BUFFER_SIZE)
    : NativeKeyListener, NativeMouseMotionListener {
  private val currentData = DataPoint()
  private var data = arrayOfNulls<DataPoint>(bufferSize)
  private var dataPointer = 0

  private val lastMousePosition = Point(0, 0)
  private var totalActions = 0L
  private var totalPixels = 0L
  private var lastTime = Date().time
  private var thisTime = 0L

  /**
   * The buffer size to determine averages for the statistics. Each [getCurrentData]
   * call uses a single frame in the buffer. Theoretically, [getCurrentData] is called every
   * animation frame redraw, which is equivalent to the user's monitor's refresh rate. Hence,
   * on a 60Hz monitor, a buffer size of 60 would be a 1 second window.
   */
  var bufferSize: Int
    get() = data.size
    set(bufferSize) {
      if (bufferSize < 0) {
        return
      }
      synchronized(currentData) {
        data = arrayOfNulls(bufferSize)
        dataPointer = 0
      }
    }

  init {
    // Disable JNativeHook's very verbose logging
    val globalScreenLogger = Logger.getLogger(GlobalScreen::class.java.`package`.name)
    globalScreenLogger.level = Level.WARNING
    globalScreenLogger.useParentHandlers = false

    // Add hooks to JNativeHook
    synchronized(jNativeHookInstancesSynchronizer) {
      if (jNativeHookInstances == 0) {
        try {
          GlobalScreen.registerNativeHook()
          ++jNativeHookInstances
        } catch (e: NativeHookException) {
          LOGGER.log(Level.SEVERE, "Couldn't start JNativeHook.", e)
          Alert(
              Alert.AlertType.WARNING,
              JNATIVEHOOK_NO_START_ALERT_BODY + e.message,
              ButtonType.OK)
              .showAndWait()
        }
      }
    }

    GlobalScreen.addNativeKeyListener(this)
    GlobalScreen.addNativeMouseMotionListener(this)
  }

  /** Returns the current data metrics for the most recent time slice */
  fun getCurrentData(): CurrentData {
    // Calculate results
    var delta = 0.0
    var actions: Long = 0
    var movement: Long = 0
    synchronized(currentData) {
      for (data in data) {
        if (data == null) {
          continue
        }
        delta += data.timeDeltaMS.toDouble()
        actions += data.totalKeyboardActions.toLong()
        movement += data.totalMouseMovePixels
      }
    }
    delta /= 60.0 * 1000 // 1 minute

    return CurrentData(
        (actions / delta).toInt(), // APM
        (movement / delta).toLong(), // PPM
        totalActions,
        totalPixels)
  }

  /** Unregisters this object from JNativeHook and shuts it down if it's the last listener. */
  fun shutdown() {
    GlobalScreen.removeNativeKeyListener(this)
    GlobalScreen.removeNativeMouseMotionListener(this)

    synchronized(jNativeHookInstancesSynchronizer) {
      if (jNativeHookInstances == 1) {
        try {
          GlobalScreen.unregisterNativeHook()
          --jNativeHookInstances
        } catch (e: NativeHookException) {
          LOGGER.log(Level.SEVERE, "Couldn't stop JNativeHook.", e)
          Alert(Alert.AlertType.WARNING,
              JNATIVEHOOK_NO_END_ALERT_BODY + e.message,
              ButtonType.OK)
              .showAndWait()
        }
      }
    }
  }

  override fun nativeKeyTyped(nativeKeyEvent: NativeKeyEvent) {}

  override fun nativeKeyReleased(nativeKeyEvent: NativeKeyEvent) {}

  override fun nativeMouseDragged(nativeMouseEvent: NativeMouseEvent) {}

  override fun nativeMouseMoved(nativeMouseEvent: NativeMouseEvent) {
    synchronized(currentData) {
      currentData.totalMouseMovePixels +=
          lastMousePosition.distance(nativeMouseEvent.point).toLong()
      lastMousePosition.location = nativeMouseEvent.point
    }
  }

  override fun nativeKeyPressed(nativeKeyEvent: NativeKeyEvent) {
    synchronized(currentData) {
      currentData.totalKeyboardActions++
    }
  }

  /**
   * Take a snapshot of input metrics. That is, the delta time between ticks, and the total mouse
   * and keyboard actions that occurred between.
   */
  fun tick() {
    synchronized(currentData) {
      if (data[dataPointer] == null) {
        data[dataPointer] = DataPoint()
      }

      val dataPoint = data[dataPointer]!!

      thisTime = Date().time
      dataPoint.totalKeyboardActions = currentData.totalKeyboardActions
      dataPoint.totalMouseMovePixels = currentData.totalMouseMovePixels
      totalActions += currentData.totalKeyboardActions.toLong()
      totalPixels += currentData.totalMouseMovePixels
      currentData.totalKeyboardActions = 0
      currentData.totalMouseMovePixels = 0
      dataPoint.timeDeltaMS = (thisTime - lastTime).toInt()
    }

    lastTime = thisTime

    if (dataPointer + 1 >= data.size) {
      dataPointer = 0
    } else {
      ++dataPointer
    }
  }

  companion object {
    private val LOGGER = Logger.getLogger(KeyboardAndMouseStatistics::class.java.name)
    private val JNATIVEHOOK_NO_START_ALERT_BODY =
        "Couldn't start JNativeHook due to the following error: "
    private val JNATIVEHOOK_NO_END_ALERT_BODY =
        "Couldn't shut down JNativeHook due to the following error: "
    val DEFAULT_BUFFER_SIZE = 60 * 5 // minutes

    private val jNativeHookInstancesSynchronizer = Any()
    private var jNativeHookInstances = 0
  }

  /**
   * Represents a time at which user input was polled containing the delta time between the last
   * polling and the total keyboard and mouse actions that have occurred between the two samples.
   */
  data class DataPoint(
      var timeDeltaMS: Int = 0,
      var totalKeyboardActions: Int = 0,
      var totalMouseMovePixels: Long = 0
  )

  /** A pre-calculated return object. */
  data class CurrentData(
      val keyboardAPM: Int,
      val mousePPM: Long,
      val totalActions: Long,
      val totalPixels: Long)
}
