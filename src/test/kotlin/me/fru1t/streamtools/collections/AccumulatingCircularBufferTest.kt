package me.fru1t.streamtools.collections

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.time.Clock
import java.time.Duration
import java.time.Instant

@RunWith(JUnit4::class)
class AccumulatingCircularBufferTest {
  private companion object {
    private const val TEST_BUFFER_SIZE = 5
    private val TEST_START_TIME_INSTANT = Instant.ofEpochMilli(100)
    private val TEST_ACCUMULATION_DURATION = Duration.ofMillis(50)
  }

  private val mockClock: Clock = mock {
    on { instant() } doReturn TEST_START_TIME_INSTANT
  }
  private lateinit var buffer: AccumulatingCircularBuffer<Int>

  @Before
  fun setUp() {
    buffer = AccumulatingCircularBuffer(
      size = TEST_BUFFER_SIZE,
      accumulationDuration = TEST_ACCUMULATION_DURATION,
      clock = mockClock,
      default = 0,
      reduce = Int::plus
    )
  }

  @Test
  fun insertAndAccumulateWithoutFillingBuffer() {
    buffer.addAll(arrayOf(1, 1))

    val result = buffer.iterator().asSequence().toCollection(mutableListOf())

    assertThat(result).containsExactly(0, 0, 0, 0, 2).inOrder()
  }

  @Test
  fun insertAndAccumulateOverwrite() {
    // Add data at TEST_BUFFER_SIZE + 1 increments
    for (i in 0..TEST_BUFFER_SIZE) {
      whenever(mockClock.instant()).thenReturn(
        TEST_START_TIME_INSTANT.plus(TEST_ACCUMULATION_DURATION.multipliedBy(i.toLong()))
      )
      // Test the accumulation logic
      buffer.addAll(arrayOf(i, i))
    }

    val result = buffer.iterator().asSequence().toCollection(mutableListOf())

    assertThat(result).containsExactly(2, 4, 6, 8, 10).inOrder()
  }

  @Test
  fun insertAfterAccumulationDuration() {
    buffer.add(10)
    // Skip over 1 bucket (ie. move forward 2 bucket's worth of time)
    whenever(mockClock.instant()).thenReturn(
      TEST_START_TIME_INSTANT.plus(TEST_ACCUMULATION_DURATION.multipliedBy(2).plusMillis(1))
    )
    buffer.add(20)

    val result = buffer.iterator().asSequence().toCollection(mutableListOf())

    // Backwards order, starting from most recently inserted
    assertThat(result).containsExactly(0, 0, 10, 0, 20).inOrder()
  }

  @Test
  fun insertAfterEntireBufferExpires() {
    buffer.add(10)
    // Skip over buffer size duration
    whenever(mockClock.instant()).thenReturn(
      TEST_START_TIME_INSTANT.plus(TEST_ACCUMULATION_DURATION.multipliedBy(TEST_BUFFER_SIZE.toLong()).plusMillis(1))
    )

    val result = buffer.iterator().asSequence().toCollection(mutableListOf())

    assertThat(result).containsExactly(0, 0, 0, 0, 0).inOrder()
  }
}