package me.fru1t.streamtools;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents generic settings with several methods. By default, settings contain window width
 * and height.
 * @param <T> The settings subclass type.
 */
public abstract class Settings<T> {
    private static final Logger LOGGER = Logger.getLogger(Settings.class.getName());

    /**
     * Provides a copy of these settings.
     * @return A copy of these settings.
     */
    public final T copy() {
        // This method uses reflection to grab all the fields from the class that extended this
        // Settings class, then populates a new instance of the class with those fields from this
        // settings object.

        // Create a list of the fields for the class
        ArrayList<Class<?>> constructorClassList = new ArrayList<>();
        ArrayList<Object> constructorParameters = new ArrayList<>();
        try {
            for (Field field : getClass().getDeclaredFields()) {
                if (!Modifier.isStatic(field.getModifiers())) {
                    if (!Modifier.isPublic(field.getModifiers())) {
                        field.setAccessible(true);
                    }

                    constructorClassList.add(field.getType());
                    constructorParameters.add(field.get(this));
                }
            }
        } catch (IllegalAccessException e) {
            LOGGER.log(Level.SEVERE, "Tried to fetch fields to create a copy of "
                    + getClass().getName() + " but couldn't: " + e.getMessage());
            throw new RuntimeException("Tried to fetch fields to create a copy of "
                    + getClass().getName() + " but couldn't: " + e.getMessage());
        }

        // See if there exists a constructor with those parameters
        Constructor<T> constructor;
        try {
            //noinspection unchecked
            constructor = (Constructor<T>) getClass().getDeclaredConstructor(
                    constructorClassList.toArray(new Class<?>[constructorClassList.size()]));
        } catch (NoSuchMethodException e) {
            LOGGER.log(Level.SEVERE, "Tried to find a constructor for " + getClass().getName()
                    + " but couldn't find one that had all non-static fields in the order they "
                    + "were declared.");
            throw new RuntimeException("Tried to find a constructor for " + getClass().getName()
                    + " but couldn't find one that had all non-static fields in the order they "
                    + "were declared.");
        }

        // Make sure it's visible to us
        if (!Modifier.isPublic(constructor.getModifiers())) {
            constructor.setAccessible(true);
        }

        // Create the object
        T result;
        try {
            result = constructor.newInstance(constructorParameters.toArray());
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            LOGGER.log(Level.SEVERE, "Tried to create a copy of " + getClass().getName()
                    + " but failed to instantiate a new copy: " + e.getMessage());
            throw new RuntimeException("Tried to create a copy of " + getClass().getName()
                    + " but failed to instantiate a new copy: " + e.getMessage());
        }

        return result;
    }

    /**
     * Updates this settings object to the given one.
     * @param settings The settings object to update to.
     */
    public final void update(Settings<?> settings) {
        if (!getClass().isAssignableFrom(settings.getClass())) {
            LOGGER.log(Level.WARNING, "Attempted to update " + getClass().getName() + " with " +
                    settings.getClass() + ". Ignoring the update.");
            return;
        }

        try {
            // Set all subclass fields
            Field[] fields = getClass().getDeclaredFields();
            for (Field field : fields) {
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }

                if (!Modifier.isPublic(field.getModifiers())) {
                    field.setAccessible(true);
                }
                field.set(this, field.get(settings));
            }
        } catch (IllegalAccessException e) {
            LOGGER.log(Level.SEVERE, "Tried to update setting for " + getClass().getName()
                    + " but erred with: " + e.getMessage());
        }
    }
}
