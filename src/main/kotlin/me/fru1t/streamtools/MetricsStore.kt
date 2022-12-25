package me.fru1t.streamtools

import com.github.kwhat.jnativehook.GlobalScreen
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent
import com.github.kwhat.jnativehook.mouse.NativeMouseListener
import me.fru1t.streamtools.collections.AccumulatingCircularBuffer
import me.fru1t.streamtools.collections.ExpiringCircularBuffer
import java.time.Clock
import java.time.Duration
import java.time.Instant

/** Tracks and stores metrics used within the StreamTools window. */
class MetricsStore : NativeKeyListener, NativeMouseListener {
  companion object {
    private val actionKeyCodes: Set<Int> = hashSetOf(
      17, // W
      30, // A
      31, // S
      32, // D
      29, // Left CTRL (crouch)
      57, // Space (Jump)

      16, // Q (last weapon)
      18, // E (reload)
      19, // R (melee)
      20, // T (interact)

      33, // F (Ability 1)
      42, // LShift (Ability 2)
    )
    private val actionMouseCodes: Set<Int> = hashSetOf(
      1, // Left click
      2, // Right click
      5, // Mouse5 (ability 3)
      3, // Scroll wheel (ping)
    )
    private val movementKeyCodes: Set<Int> = hashSetOf(
      17, // W
      30, // A
      31, // S
      32, // D
      29, // Left CTRL (crouch)
      57, // Space (Jump)
    )
    private val clickKeyCodes: Set<Int> = hashSetOf(
      1, // Left click
      2, // Right click
    )

    // Capping out at ~1000APM even while spamming more than you would in a game. This is ~16APS. Double this to have a
    // reasonably impossible cap to reach. To be fair, we could make this buffer in the thousands and still not see a
    // performance impact, but :shrug:. This number should scale up linearly with the below expiration (in this case
    // ~30/second), but you can restrict or loosen this as much as you want.
    private const val LAST_ACTIONS_BUFFER_SIZE = 32
    // 1 second is just long enough to have the information stick around to be read, but short enough that the graphics
    // don't feel artificially inflated/laggy. This is all subjective, of course.
    private val LAST_ACTIONS_BUFFER_EXPIRATION_DURATION = Duration.ofMillis(1000)

    // This translates into how many bars in the graph there are for the historical APM.
    const val HISTORICAL_APM_BUFFER_SIZE = 180
    private val HISTORICAL_APM_ACCUMULATION_DURATION = Duration.ofSeconds(5)

    private const val LAST_CLICKS_BUFFER_SIZE = 20
    private val LAST_CLICKS_BUFFER_EXPIRATION_DURATION = Duration.ofMillis(3000)

    private const val LAST_MOVEMENT_BUFFER_SIZE = 40
    private val LAST_MOVEMENT_BUFFER_EXPIRATION_DURATION = Duration.ofMillis(3000)


    /**
     * Calculates an instantaneous "n" per minute by averaging the time between actions and extrapolating how many can
     * fit in 60 seconds.
     */
    private fun getInstantIntPerMinute(buffer: Iterable<Instant>): Int {
      var samples = 0
      var totalDelay = 0L
      var lastInput = 0L
      buffer.iterator().forEach {
        if (lastInput != 0L) {
          samples++
          totalDelay += it.toEpochMilli() - lastInput
        }
        lastInput = it.toEpochMilli()
      }
      if (samples == 0) {
        return 0
      }

      return (60000.0 * samples / totalDelay).toInt()
    }
  }

  private val clock = Clock.systemDefaultZone()

  private val pressedKeys: MutableMap<Int, Boolean> = hashMapOf()
  private val lastActionsBuffer: ExpiringCircularBuffer<Instant> =
    ExpiringCircularBuffer(LAST_ACTIONS_BUFFER_SIZE, LAST_ACTIONS_BUFFER_EXPIRATION_DURATION, clock)
  // Stores the sum of deltas between action presses
  private val historicalApmBuffer: AccumulatingCircularBuffer<Long> = AccumulatingCircularBuffer(
    HISTORICAL_APM_BUFFER_SIZE, HISTORICAL_APM_ACCUMULATION_DURATION, clock, 0L, Long::plus
  ) { reducedData, entries -> (60000.0 / (reducedData / entries)).toLong() }
  private var lastActionTime: Long = -1L
  private var currentActionTime: Long = -1L
  private val lastClicksBuffer: ExpiringCircularBuffer<Instant> =
    ExpiringCircularBuffer(LAST_CLICKS_BUFFER_SIZE, LAST_CLICKS_BUFFER_EXPIRATION_DURATION, clock)
  private val lastMovementBuffer: ExpiringCircularBuffer<Instant> =
    ExpiringCircularBuffer(LAST_MOVEMENT_BUFFER_SIZE, LAST_MOVEMENT_BUFFER_EXPIRATION_DURATION, clock)
  private val historicalActionsAccumulationArray: ArrayList<Long> = ArrayList(HISTORICAL_APM_BUFFER_SIZE)
  private var totalActions = 0
  private var totalMouseClicks = 0

  init {
    GlobalScreen.addNativeKeyListener(this)
    GlobalScreen.addNativeMouseListener(this)
  }

  /**
   * Calculates an instantaneous APM by averaging the time between actions and extrapolating how many can fit in 60
   * seconds. The number of samples taken is configured by the `lastActionsBuffer`, but requires at least 2 actions in
   * the buffer.
   */
  fun getInstantApm(): Int = getInstantIntPerMinute(lastActionsBuffer)

  /** Calculates an instantaneous clicks per minute with the same algorithm as the above APM calculation. */
  fun getInstantClicksPerMinute(): Int = getInstantIntPerMinute(lastClicksBuffer)

  /** Calculates an instantaneous clicks per minute with the same algorithm as the above APM calculation. */
  fun getInstantMovementPerMinute(): Int = getInstantIntPerMinute(lastMovementBuffer)

  /** Returns the total number of action keys pressed this session. */
  fun getTotalActions(): Int = totalActions

  /** Returns the total number of mouse clicks pressed this session. */
  fun getTotalMouseClicks(): Int = totalMouseClicks

  /** Returns the historical number of actions as counted by the buffer. */
  fun getHistoricalActions(): ArrayList<Long> {
    historicalActionsAccumulationArray.clear()
    historicalApmBuffer.iterator().asSequence().toCollection(historicalActionsAccumulationArray)
    return historicalActionsAccumulationArray
  }

  override fun nativeKeyPressed(e: NativeKeyEvent) {
    // Save memory by ignoring keys outside the scope of the actions. Not actually that many, whatever.
    if (!actionKeyCodes.contains(e.keyCode)) {
      return
    }

    // Prevent a held key from spamming the buffer
    if (pressedKeys[e.keyCode] == false) {
      lastActionsBuffer.add(clock.instant())
      addToHistoricalActions()
      ++totalActions

      if (movementKeyCodes.contains(e.keyCode)) {
        lastMovementBuffer.add(clock.instant())
      }
    }
    pressedKeys[e.keyCode] = true
  }

  override fun nativeKeyReleased(e: NativeKeyEvent) {
    if (!actionKeyCodes.contains(e.keyCode)) {
      return
    }
    pressedKeys[e.keyCode] = false
  }

  override fun nativeMousePressed(e: NativeMouseEvent) {
    if (actionMouseCodes.contains(e.button)) {
      lastActionsBuffer.add(clock.instant())
      addToHistoricalActions()
      ++totalActions
    }

    if (clickKeyCodes.contains(e.button)) {
      lastClicksBuffer.add(clock.instant())
      ++totalMouseClicks
    }
  }

  private fun addToHistoricalActions() {
    currentActionTime = clock.instant().toEpochMilli()
    if (lastActionTime > 0) {
      historicalApmBuffer.add(currentActionTime - lastActionTime)
    }
    lastActionTime = currentActionTime
  }
}
