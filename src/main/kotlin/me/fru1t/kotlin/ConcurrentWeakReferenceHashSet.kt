package me.fru1t.kotlin

import java.lang.ref.WeakReference
import java.util.Objects
import javax.annotation.concurrent.GuardedBy

/** A thread-safe [HashSet] that stores [WeakReference]s to all values [add]ed. */
class ConcurrentWeakReferenceHashSet<E> {
  @GuardedBy("set")
  private val set: HashSet<WeakReference<E>> = HashSet()

  /** Adds [e] to this set if not already present. */
  fun add(e: E): Boolean = synchronized(set, { set.add(WeakReference(e)) })

  /** Removes all elements from this set. */
  fun clear() = synchronized(set, { set.clear() })

  /** Returns `true` if this set contains [e], otherwise `false`. */
  fun contains(e: E): Boolean {
    var result = false
    forEach {
      if (Objects.equals(e, it)) {
        result = true
      }
    }
    return result
  }

  /** Returns `true` if this set contains no elements, otherwise `false`.*/
  fun isEmpty(): Boolean = size() == 0

  /** Removes [e] from this set if it present. */
  fun remove(e: E): Boolean {
    synchronized(set, {
      val iterator = set.iterator()
      while (iterator.hasNext()) {
        val value = iterator.next().get()
        if (Objects.equals(e, value)) {
          iterator.remove()
          return true
        }
      }
    })
    return false
  }

  /** Returns the number of elements in this set (its cardinality). */
  fun size(): Int {
    var result = 0
    forEach { ++result }
    return result
  }

  /** Iterates over all non-null values in this set. */
  fun forEach(operation: (E) -> Unit) {
    synchronized(set, {
      val iterator = set.iterator()
      while (iterator.hasNext()) {
        val value = iterator.next().get()
        if (value == null) {
          iterator.remove()
        } else {
          operation.invoke(value)
        }
      }
    })
  }
}
