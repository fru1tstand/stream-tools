package me.fru1t.streamtools.collections

import java.time.Clock
import java.time.Duration
import java.time.Instant

/**
 * A fixed sized buffer that accumulates inputs into a reduced, single element, moving onto the next element after a
 * specified time. This buffer does not support removal of elements, but writes over old elements once filled up,
 * keeping a fixed sized buffer. In database terms, this is an aggregation, grouped by time. **Iteration through the
 * values of this buffer are from oldest to newest entries**.
 *
 * This buffer always returns the number of elements as specified by its size, providing a default value for elements
 * that may not have actually been used yet.
 *
 * Example usage: tracking minute-by-minute events over the last 30 minutes.
 *
 * ```
 * AccumulatingCircularBuffer<Int>(
 *   size = 30, // 30 buckets of 60 seconds
 *   accumulationDuration = Duration.ofSeconds(60),
 *   clock = Clock.systemDefaultZone(),
 *   default = 0, // 0 events by default
 *   reduce: Int::plus) // Reduce (aggregate) by adding the values
 * ```
 */
class AccumulatingCircularBuffer<E : Any>(
  override val size: Int,
  private val accumulationDuration: Duration,
  private val clock: Clock,
  private val default: E,
  private val reduce: (existing: E, new: E) -> E
) : MutableCollection<E> {
  private inner class Item<E : Any>(var data: E, var expirationInstant: Instant) {
    lateinit var next: Item<E>

    /** Returns if this element should no longer be appended to due to the expiration time passing. */
    fun hasExpired(): Boolean = clock.instant().isAfter(expirationInstant)
  }

  /**
   * Iterates through from the given [head] all the way around without repeating, assuming the list is linked in a
   * circular graph.
   */
  private class SimpleListIterator<E : Any>(private val head: AccumulatingCircularBuffer<E>.Item<E>) :
    MutableIterator<E> {
    // Treat the first element as special so we can mark that we've done a complete loop through the linked list
    private var current: AccumulatingCircularBuffer<E>.Item<E>? = null
    private lateinit var nextReturnCache: AccumulatingCircularBuffer<E>.Item<E>

    override fun hasNext(): Boolean = current != head

    override fun next(): E {
      if (!hasNext()) {
        throw IllegalStateException("No next element to return")
      }

      // This is a tricky statement. Effectively, this skips over having `current` ever be the same as `head` UNTIL
      // we've looped back around to it
      nextReturnCache = current ?: head
      current = nextReturnCache.next
      return nextReturnCache.data
    }

    override fun remove() = throw UnsupportedOperationException()
  }

  // The true "head" position should be updated every time we want to read or write to this data structure.
  private var _head: Item<E> = Item(default, clock.instant().plus(accumulationDuration))
  private val head: Item<E>
    get() {
      // It's possible that the next time this data structure is accessed is AFTER the next "bucket" of accumulation has
      // already expired. To catch the general case, we'll incrementally update all buckets between the last access and
      // now until everything is updated
      var newExpirationInstant: Instant
      while (true) {
        if (!_head.hasExpired()) {
          return _head
        }

        newExpirationInstant = _head.expirationInstant.plus(accumulationDuration)
        _head = _head.next
        _head.data = default
        _head.expirationInstant = newExpirationInstant
      }
    }

  init {
    val firstElement = _head
    var temp: Item<E>
    for (i in 1 until size) {
      temp = Item(default, Instant.EPOCH)
      _head.next = temp
      _head = _head.next
    }
    _head.next = firstElement
    _head = firstElement
  }

  override fun add(element: E): Boolean {
    head.data = reduce(head.data, element)
    return true
  }

  override fun addAll(elements: Collection<E>): Boolean {
    elements.forEach(this::add)
    return true
  }

  override fun isEmpty(): Boolean = false

  override fun iterator(): MutableIterator<E> = SimpleListIterator(head.next)

  // Unsupported operations. I mean, you /can/ implement these. But they're unused in StreamTools.
  override fun clear() = throw UnsupportedOperationException()
  override fun contains(element: E): Boolean = throw UnsupportedOperationException()
  override fun containsAll(elements: Collection<E>): Boolean = throw UnsupportedOperationException()
  override fun retainAll(elements: Collection<E>): Boolean = throw UnsupportedOperationException()
  override fun removeAll(elements: Collection<E>): Boolean = throw UnsupportedOperationException()
  override fun remove(element: E): Boolean = throw UnsupportedOperationException()
}