package me.fru1t.streamtools.controller

import java.lang.reflect.Constructor
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Modifier
import java.util.ArrayList
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Base settings class that provides [copy] and mutable [update] methods. Requires the type [T]
 * which [copy] will return. Extending classes should be a data class, but isn't strictly enforced.
 */
abstract class Settings<out T> {

  /** Returns a copy of these settings as [T]. */
  fun copy(): T {
    // This method uses reflection to grab all the fields from the class that extended this
    // Settings class, then populates a new instance of the class with those fields from this
    // settings object. Kotlin's data classes will handle this automatically.

    // Create a list of the fields for the class
    val constructorClassList = ArrayList<Class<*>>()
    val constructorParameters = ArrayList<Any>()
    try {
      for (field in javaClass.declaredFields) {
        if (!Modifier.isStatic(field.modifiers)) {
          if (!Modifier.isPublic(field.modifiers)) {
            field.isAccessible = true
          }

          constructorClassList.add(field.type)
          constructorParameters.add(field.get(this))
        }
      }
    } catch (e: IllegalAccessException) {
      throw RuntimeException("Tried to fetch fields to create a copy of " + javaClass.name + " but couldn't: " + e.message, e)
    }

    // See if there exists a constructor with those parameters
    val constructor: Constructor<T>
    try {
      @Suppress("UNCHECKED_CAST")
      constructor =
          javaClass.getDeclaredConstructor(*constructorClassList.toTypedArray()) as Constructor<T>
    } catch (e: NoSuchMethodException) {
      throw RuntimeException("${javaClass.name} must be a data class", e)
    }

    // Make sure it's visible to us
    if (!Modifier.isPublic(constructor.modifiers)) {
      constructor.isAccessible = true
    }

    // Create the object
    val result: T
    try {
      result = constructor.newInstance(*constructorParameters.toTypedArray())
    } catch (e: InstantiationException) {
      throw RuntimeException("Failed to create a copy of ${javaClass.name}: ${e.message}", e)
    } catch (e: IllegalAccessException) {
      throw RuntimeException("Failed to create a copy of ${javaClass.name}: ${e.message}", e)
    } catch (e: InvocationTargetException) {
      throw RuntimeException("Failed to create a copy of ${javaClass.name}: ${e.message}", e)
    }

    return result
  }

  /** Updates these [Settings] to [settings] by making a defensive copy of all the fields. */
  fun update(settings: Settings<*>) {
    if (!javaClass.isAssignableFrom(settings.javaClass)) {
      LOGGER.log(Level.SEVERE, "Cannot update ${javaClass.name} from ${settings.javaClass.name}")
      return
    }

    try {
      // Set all subclass fields
      val fields = javaClass.declaredFields
      for (field in fields) {
        if (Modifier.isStatic(field.modifiers)) {
          continue
        }

        if (!Modifier.isPublic(field.modifiers)) {
          field.isAccessible = true
        }

        val newFieldValue = field.get(settings)
        if (newFieldValue != null) {
          field.set(this, newFieldValue)
        } else {
          LOGGER.log(Level.INFO, "Ignoring update on null field ${javaClass.name}#${field.name}")
        }
      }
    } catch (e: IllegalAccessException) {
      LOGGER.log(
          Level.SEVERE,
          "Tried to update setting for ${javaClass.name} but erred with: ${e.message}")
    }

  }

  companion object {
    private val LOGGER = Logger.getLogger(Settings::class.java.name)
  }
}
