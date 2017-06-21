package me.fru1t.streamtools;

import lombok.Getter;

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

    protected @Getter double windowWidth;
    protected @Getter double windowHeight;

    public T setWindowWidth(double width) {
        windowWidth = width;
        //noinspection unchecked
        return (T) this;
    }

    public T setWindowHeight(double height) {
        windowHeight = height;
        //noinspection unchecked
        return (T) this;
    }

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

        // Set window settings
        if (result instanceof Settings) {
            Settings settingsResult = (Settings) result;
            settingsResult.windowHeight = this.windowHeight;
            settingsResult.windowWidth = this.windowWidth;
        } else {
            // This should never occur
            throw new RuntimeException("The result " + result.getClass().getName() + " isn't of "
                    + "the Settings type. How did this happen??");
        }

        return result;
    }

    /**
     * Updates this settings object to the given one.
     * @param settings The settings object to update to.
     */
    public final void update(T settings) {
        try {
            // Set all subclass fields
            Field[] fields = getClass().getDeclaredFields();
            for (Field field : fields) {
                if (!Modifier.isPublic(field.getModifiers())) {
                    field.setAccessible(true);
                }
                field.set(this, field.get(settings));
            }

            // Set window fields
            if (settings instanceof Settings) {
                Settings otherSettings = (Settings) settings;
                windowHeight = otherSettings.windowHeight;
                windowWidth = otherSettings.windowWidth;
            }
        } catch (IllegalAccessException e) {
            LOGGER.log(Level.SEVERE, "Tried to update setting for " + getClass().getName()
                    + " but errored with: " + e.getMessage());
            throw new RuntimeException("Tried to update setting for " + getClass().getName()
                    + " but errored with: " + e.getMessage());
        }
    }
}
