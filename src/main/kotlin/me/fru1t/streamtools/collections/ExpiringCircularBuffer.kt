package me.fru1t.streamtools.collections

import java.lang.IllegalStateException
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.Collections

/**
 * A fixed sized buffer that lazily evicts elements if they've expired, or overwrites old elements if the buffer has
 * filled. This buffer is optimized for inserts and expirations at O(1) with search (even for index 0) at O(n).
 *
 * The size of the buffer is actually `size + 1`, leaving the last element inaccessible. This reduces the number of
 * edge cases when finding the "head" of the collection (after elements have expired). This leaves 2 states the buffer
 * can be in:
 *
 * 1. Empty (when the collection is empty, tail is set to `head`)
 *    ```
 *         [head, tail)
 *         v
 *       #######################
 *    -> # 1 # 2 # 3 # n # n+1 # --
 *    |  #######################  |
 *    ----------------------------
 *    ```
 * 2. Partially filled (with or without expired elements). The underlying data structure can never be truly "filled"
 *    (ie. that the head and tail are the same) because there will always be the `n+1` element that the tail will
 *    write to.
 *    ```
 *         [head,                    tail)
 *         v                         v
 *       #################################
 *    -> # 1 (expired) # 2 # 3 # n # n+1 # --
 *    |  #################################  |
 *    --------------------------------------
 *    ```
 *
 * **Inserts** will always write to the tail and move the tail pointer once. After the tail has moved, if the head also
 * points to the same element, the head will be moved forward 1. This keeps a size of `n` elements, and ignores whether
 * the head is pointing to an expired item (which doesn't matter for the case of insert).
 *
 *  - State 1 (after insert): `head = 1, tail = 2`
 *  - State 2 (after insert): `head = 2, tail = 1`
 *
 * **Iterator** will start at the head pointer and traverse down to the tail until it finds the first non-expired
 * element. The head pointer will be shifted to this element, and the iterator will start until it reaches the tail,
 * but does not include the tail. If there are no valid elements, no items are returned by the iterator.
 *
 *  - State 1 (before an insert): `[]`
 *  - State 2 (before an insert): `[2, 3, n]`
 *  - State 1 (after an insert): `[1]`
 *  - State 2 (after an insert): `[2, 3, n, n+1]`
 */
class ExpiringCircularBuffer<E : Any>(
  override val size: Int,
  private val expiration: Duration,
  private val clock: Clock
) :
  MutableCollection<E> {
  private inner class Item<E : Any>(var expirationInstant: Instant) {
    lateinit var next: Item<E>
    lateinit var data: E

    /**
     * Returns if this element should no longer be counted as part of the collection due to the expiration time passing.
     */
    fun hasExpired(): Boolean = clock.instant().isAfter(expirationInstant)
  }

  /**
   * Iterates through from the given [head] to [tail] with the assumption that they're connected, and any prerequisite
   * filtering is already done (i.e. checking for expiration).
   */
  private class SimpleListIterator<E : Any>(
    private var head: ExpiringCircularBuffer<E>.Item<E>,
    private val tail: ExpiringCircularBuffer<E>.Item<E>
  ) : MutableIterator<E> {
    private lateinit var nextReturnCache: ExpiringCircularBuffer<E>.Item<E>

    override fun hasNext(): Boolean {
      return head != tail
    }

    override fun next(): E {
      if (!hasNext()) {
        throw IllegalStateException("No next element to return")
      }

      nextReturnCache = head
      head = head.next
      return nextReturnCache.data
    }

    override fun remove() {
      throw UnsupportedOperationException("ExpiringCircularBuffer does not support removal of individual items.")
    }
  }

  private var head: Item<E> = Item(Instant.MIN)
  private var tail: Item<E>

  init {
    tail = head
    for (i in 1 .. size) {
      tail.next = Item(Instant.MIN)
      tail = tail.next
    }
    tail.next = head
    tail = head
  }


  override fun add(element: E): Boolean {
    synchronized(tail) {
      tail.data = element
      tail.expirationInstant = clock.instant().plus(expiration)
      tail = tail.next

      // Maintain `size` elements
      if (head == tail) {
        head = head.next
      }
    }
    return true
  }

  override fun addAll(elements: Collection<E>): Boolean {
    for (element in elements) {
      add(element)
    }
    return true
  }

  override fun clear() {
    synchronized(tail) {
      head = tail
    }
  }

  // Main point of entry for this data structure to calculate where the head actually is
  override fun iterator(): MutableIterator<E> {
    if (head == tail) {
      return Collections.emptyIterator()
    }

    synchronized(tail) {
      // Find the next non-expired element
      while (head.hasExpired()) {
        head = head.next

        // Collection is empty if we reached the tail without finding a non-expired element. The tail element itself
        // does not need to be checked because it is the "n+1"th element which is not part of the collection anymore.
        if (head == tail) {
          return Collections.emptyIterator()
        }
      }

      return SimpleListIterator(head, tail)
    }
  }
  // These methods must calculate if there are any expired elements. Fetching the iterator forces this.
  override fun isEmpty(): Boolean = !iterator().hasNext()
  override fun contains(element: E): Boolean = iterator().asSequence().contains(element)

  // For the purposes of StreamTools, these are not needed
  override fun containsAll(elements: Collection<E>): Boolean = throw UnsupportedOperationException()
  override fun retainAll(elements: Collection<E>): Boolean = throw UnsupportedOperationException()
  override fun removeAll(elements: Collection<E>): Boolean = throw UnsupportedOperationException()
  override fun remove(element: E): Boolean = throw UnsupportedOperationException()
}
