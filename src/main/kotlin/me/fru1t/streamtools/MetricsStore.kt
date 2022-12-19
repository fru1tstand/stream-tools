package me.fru1t.streamtools

import com.github.kwhat.jnativehook.GlobalScreen
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent
import com.github.kwhat.jnativehook.mouse.NativeMouseListener
import me.fru1t.streamtools.collections.ExpiringCircularBuffer
import java.time.Clock
import java.time.Duration
import java.time.Instant

/** Tracks and stores metrics used within the StreamTools window. */
class MetricsStore : NativeKeyListener, NativeMouseListener {
  private companion object {
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

    // Capping out at ~1000APM even while spamming more than you would in a game. This is ~16APS. Double this to have a
    // reasonably impossible cap to reach. To be fair, we could make this buffer in the thousands and still not see a
    // performance impact, but :shrug:. This number should scale up linearly with the below expiration (in this case
    // ~30/second), but you can restrict or loosen this as much as you want.
    private const val LAST_ACTIONS_BUFFER_SIZE = 32
    // 1 second is just long enough to have the information stick around to be read, but short enough that the graphics
    // don't feel artificially inflated/laggy. This is all subjective, of course.
    private val LAST_ACTIONS_BUFFER_EXPIRATION_DURATION = Duration.ofMillis(1000)
  }

  private val clock = Clock.systemDefaultZone()

  private val lastActionsBuffer: ExpiringCircularBuffer<Instant> =
    ExpiringCircularBuffer(LAST_ACTIONS_BUFFER_SIZE, LAST_ACTIONS_BUFFER_EXPIRATION_DURATION, clock)
  private val pressedKeys: MutableMap<Int, Boolean> = hashMapOf()

  init {
    GlobalScreen.addNativeKeyListener(this)
    GlobalScreen.addNativeMouseListener(this)
  }

  /**
   * Calculates an instantaneous APM by averaging the time between actions and extrapolating how many can fit in 60
   * seconds. The number of samples taken is configured by the `lastActionsBuffer`, but requires at least 2 actions in
   * the buffer.
   */
  fun getInstantApm(): Int {
    var samples = 0
    var totalDelay = 0L
    var lastInput = 0L
    lastActionsBuffer.iterator().forEach {
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

  override fun nativeKeyPressed(e: NativeKeyEvent) {
    // Save memory by ignoring keys outside the scope of the actions. Not actually that many, whatever.
    if (!actionKeyCodes.contains(e.keyCode)) {
      return
    }

    // Prevent a held key from spamming the buffer
    if (pressedKeys[e.keyCode] == false) {
      lastActionsBuffer.add(clock.instant())
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
    lastActionsBuffer.add(clock.instant())
  }
}
