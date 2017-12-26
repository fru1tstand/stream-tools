package me.fru1t.streamtools.javafx;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import me.fru1t.javafx.Controller;
import me.fru1t.streamtools.Settings;

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
    public interface EventHandler<T extends Settings<T>> {
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

        try {
            Constructor<T> settingsConstructor = settingsClass.getConstructor();
            currentSettings = settingsConstructor.newInstance();
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
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
            throw new RuntimeException(getFxmlResourcePath() + " requires a button with an fx:id "
                    + "of \"settingsCancelButton\"");
        }
        if (settingsSaveButton == null) {
            throw new RuntimeException(getFxmlResourcePath() + " requires a button with an fx:id "
                    + "of \"settingsSaveButton\"");
        }
        if (settingsApplyButton == null) {
            throw new RuntimeException(getFxmlResourcePath() + " requires a button with an fx:id "
                    + "of \"settingsApplyButton\"");
        }

        // Wire cancel and save buttons
        settingsCancelButton.setOnAction(event -> SettingsController.this.getStage().hide());
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
            SettingsController.this.getStage().hide();
        });

        // All controls should lose focus when the root is clicked
        getScene().getRoot().setOnMouseClicked(event -> getScene().getRoot().requestFocus());
    }

    @Override
    public void show() {
        refresh();
        super.show();
    }

    @Override
    public void onStageProvide(Stage stage) {
        super.onStageProvide(stage);
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
    public void update(Settings<?> settings) {
        currentSettings.update(settings);
        refresh();
    }

    /**
     * Fired when the save button is clicked. The implementor should commit all GUI changes to
     * the {@link #currentSettings} object.
     */
    protected abstract void commitSettings();

    /**
     * Updates the GUI to reflect the currentSettings object.
     */
    protected abstract void refresh();
}
