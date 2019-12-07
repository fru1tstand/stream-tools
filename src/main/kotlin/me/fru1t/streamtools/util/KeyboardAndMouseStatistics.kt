package me.fru1t.streamtools.util

import me.fru1t.slik.annotations.Inject
import me.fru1t.slik.annotations.Singleton

@Inject
@Singleton
class KeyboardAndMouseStatisticsFactory {
  /**
   * Creates a new KeyboardAndMouseStatisticsImpl object with [bufferSize] for
   * calculating averages. One buffer slot is used every [KeyboardAndMouseStatistics.getCurrentData]
   * call which should be every animation timer tick. The tick rate is different for each machine
   * and should match the user's monitor's refresh rate. See
   * [KeyboardAndMouseStatisticsImpl.bufferSize]
   */
  fun create(bufferSize: Int = KeyboardAndMouseStatisticsImpl.DEFAULT_BUFFER_SIZE) =
      KeyboardAndMouseStatisticsImpl(bufferSize)
}

/** Interface for retrieving mouse movement and keyboard strokes. */
interface KeyboardAndMouseStatistics {
  /** Returns the current data metrics for the most recent time slice */
  fun getCurrentData(): CurrentData

  /** Returns the total number of key presses for the given character. */
  fun getTotalKeyPresses(char: Char): Long

  /** A pre-calculated return object. */
  data class CurrentData(
      val keyboardAPM: Int,
      val mousePPM: Long,
      val totalActions: Long,
      val totalPixels: Long)

  /**
   * Represents a time at which user input was polled containing the delta time between the last
   * polling and the total keyboard and mouse actions that have occurred between the two samples.
   */
  data class DataPoint(
      var timeDeltaMS: Int = 0,
      var totalKeyboardActions: Int = 0,
      var totalMouseMovePixels: Long = 0
  )
}
