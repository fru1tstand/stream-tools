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
class ExpiringCircularBufferTest {
  private companion object {
    private const val TEST_BUFFER_SIZE = 5
    private val TEST_START_TIME_INSTANT = Instant.ofEpochMilli(100)
    private val TEST_EXPIRATION_DURATION = Duration.ofMillis(50)
  }

  private val mockClock: Clock = mock {
    on { instant() } doReturn TEST_START_TIME_INSTANT
  }
  private lateinit var buffer: ExpiringCircularBuffer<Int>

  @Before
  fun setUp() {
    buffer = ExpiringCircularBuffer(TEST_BUFFER_SIZE, TEST_EXPIRATION_DURATION, mockClock)
  }

  @Test
  fun insert() {
    buffer.add(1)

    val result = buffer.iterator().asSequence().toCollection(mutableListOf())

    assertThat(result).containsExactly(1)
  }

  @Test
  fun expire() {
    buffer.add(1)

    whenever(mockClock.instant()).thenReturn(TEST_START_TIME_INSTANT.plus(TEST_EXPIRATION_DURATION).plusMillis(1))
    val result = buffer.iterator().asSequence().toCollection(mutableListOf())

    assertThat(result).isEmpty()
  }

  @Test
  fun overwrite() {
    buffer.addAll(arrayOf(1, 2, 3, 4, 5, 6))

    val result = buffer.iterator().asSequence().toCollection(mutableListOf())

    assertThat(result).containsExactly(6, 2, 3, 4, 5)
  }

  @Test
  fun insertAfterExpire() {
    buffer.add(1)
    whenever(mockClock.instant()).thenReturn(TEST_START_TIME_INSTANT.plus(TEST_EXPIRATION_DURATION).plusMillis(1))
    buffer.add(2)

    val result = buffer.iterator().asSequence().toCollection(mutableListOf())

    assertThat(result).containsExactly(2)
  }

  @Test
  fun insertAfterOverwriteAndExpire() {
    buffer.addAll(arrayOf(1, 2, 3, 4, 5, 6))
    whenever(mockClock.instant()).thenReturn(TEST_START_TIME_INSTANT.plus(TEST_EXPIRATION_DURATION).plusMillis(1))
    buffer.add(7)

    val result = buffer.iterator().asSequence().toCollection(mutableListOf())

    assertThat(result).containsExactly(7)
  }
}