package me.fru1t.streamtools.javafx;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import me.fru1t.javafx.Controller;
import me.fru1t.streamtools.Settings;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.HashSet;

/**
 * A specialized controller that controls a settings panel that requires a save and cancel button.
 * @param <T> The object type passed back to event listeners.
 */
public abstract class SettingsController<T extends Settings<T>> extends Controller {
    /**
     * Possible events for the SettingsController.
     */
    public interface EventHandler<T extends Settings> {
        void onSettingsChange(T settings);
    }

    // FXML Declarations
    private @FXML Button settingsCancelButton;
    private @FXML Button settingsSaveButton;
    private @FXML Button settingsApplyButton;

    // Class declarations
    protected final T currentSettings;
    private final HashSet<EventHandler<T>> eventHandlers;

    protected SettingsController() {
        // Setup event handlers
        eventHandlers = new HashSet<>();

        // Set up currentSettings
        // Because we aren't handed any other context to the settings, we must infer the Settings
        // class through the generic T, then ask for its @DefaultSetting annotation to pull default
        // settings field for that class.

        // Grab this class (SettingsController<T>) as a parametrized type from the implementing
        // class's definition.
        ParameterizedType settingsControllerClass =
                (ParameterizedType) getClass().getGenericSuperclass();

        // Get the class type T
        @SuppressWarnings("unchecked")
        Class<T> settingsClass = (Class<T>) settingsControllerClass.getActualTypeArguments()[0];

        // Get the @DefaultSettings annotation for T
        DefaultSettings defaultSettingsAnnotation = null;
        for (Annotation annotation : settingsClass.getAnnotations()) {
            if (annotation instanceof DefaultSettings) {
                defaultSettingsAnnotation = (DefaultSettings) annotation;
                break;
            }
        }
        if (defaultSettingsAnnotation == null) {
            throw new RuntimeException("The settings class " + settingsClass.getName()
                    + " must be annotated with @DefaultSettings.");
        }

        // Get the field that the @DefaultSettings annotation points to.
        Field defaultSettingsField;
        try {
            defaultSettingsField =
                    settingsClass.getDeclaredField(defaultSettingsAnnotation.value());
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("The settings class '" + settingsClass.getName()
                    + "' denoted the field '" + defaultSettingsAnnotation.value()
                    + "' as the default settings object, but the field could not be found: "
                    + e.getMessage());
        }

        // Make sure the field we retrieved is a static final
        int modifiers = defaultSettingsField.getModifiers();
        if (!Modifier.isStatic(modifiers) || !Modifier.isFinal(modifiers)) {
            throw new RuntimeException("The default settings field '"
                    + defaultSettingsAnnotation.value() + "' in '" + settingsClass.getName()
                    + "' must be marked static and final (eg. a class constant).");
        }

        // If the field is not public, mark it accessible to us.
        if (!Modifier.isPublic(modifiers)) {
            defaultSettingsField.setAccessible(true);
        }

        // Fetch the actual value of the field (this value should be the default settings object)
        Object defaultSettingsObject;
        try {
            defaultSettingsObject = defaultSettingsField.get(null);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to fetch the field '"
                    + defaultSettingsAnnotation.value() + "' from '" + settingsClass.getName()
                    + "' due to: " + e.getMessage());
        }

        // Verify the value we fetched is of type T (the settings class)
        if (!settingsClass.isAssignableFrom(defaultSettingsObject.getClass())) {
            throw new RuntimeException("The default settings object retrieved from '"
                    + defaultSettingsAnnotation.value() + "' in class '" + settingsClass.getName()
                    + "' is not the same type as the settings class. It's a '"
                    + defaultSettingsObject.getClass() + "' instead.");
        }

        // Finally, we can assign it to our current settings object as a copy.
        //noinspection unchecked
        currentSettings = ((T) defaultSettingsObject).copy();
    }

    /**
     * When overriding this method, this parent method must be called using
     * <code>super.onSceneCreate();</code>
     */
    @Override
    protected void onSceneCreate() {
        super.onSceneCreate();

        // Check that the settings panel has both a cancel and save button action.
        if (settingsCancelButton == null) {
            throw new RuntimeException(getFXMLResourcePath() + " requires a button with an fx:id "
                    + "of \"settingsCancelButton\"");
        }
        if (settingsSaveButton == null) {
            throw new RuntimeException(getFXMLResourcePath() + " requires a button with an fx:id "
                    + "of \"settingsSaveButton\"");
        }
        if (settingsApplyButton == null) {
            throw new RuntimeException(getFXMLResourcePath() + " requires a button with an fx:id "
                    + "of \"settingsApplyButton\"");
        }

        // Wire cancel and save buttons
        settingsCancelButton.setOnAction(event -> SettingsController.this.stage.hide());
        settingsApplyButton.setOnAction(event -> {
            commitSettings();
            for (EventHandler<T> handler : eventHandlers) {
                handler.onSettingsChange(currentSettings);
            }
        });
        settingsSaveButton.setOnAction(event -> {
            commitSettings();
            for (EventHandler<T> handler : eventHandlers) {
                handler.onSettingsChange(currentSettings);
            }
            SettingsController.this.stage.hide();
        });
    }

    @Override
    public void show() {
        refresh();
        super.show();
    }

    @Override
    public void provideStage(Stage stage) {
        super.provideStage(stage);
        stage.setResizable(false);
    }

    // Adds an event handler
    public void addEventHandler(EventHandler<T> handler) {
        eventHandlers.add(handler);
    }

    // Removes an event handler
    public void removeEventHandler(EventHandler<T> handler) {
        eventHandlers.remove(handler);
    }

    /**
     * @return The currently committed settings.
     */
    public T getCurrentSettings() {
        return currentSettings.copy();
    }

    /**
     * Updates the settings to the given settings object.
     * @param settings The new settings
     */
    public void update(T settings) {
        currentSettings.update(settings);
        refresh();
    }

    /**
     * Fired when the save button is clicked. The implementor should commimt all GUI changes to
     * the {@link #currentSettings} object.
     */
    protected abstract void commitSettings();

    /**
     * Updates the GUI to reflect the currentSettings object.
     */
    protected abstract void refresh();
}
