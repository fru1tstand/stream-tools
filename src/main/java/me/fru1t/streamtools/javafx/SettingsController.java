package me.fru1t.streamtools.javafx;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import me.fru1t.javafx.Controller;
import me.fru1t.streamtools.Settings;

import javax.annotation.Nonnull;
import java.util.HashSet;

/**
 * A specialized controller that controls a settings panel that requires a save and cancel button.
 * @param <T> The object type passed back to event listeners.
 */
public abstract class SettingsController<T extends Settings> extends Controller {
    /**
     * Possible events for the SettingsController.
     */
    public interface EventHandler<T extends Settings> {
        void onChange(T settings);
    }

    // FXML Declarations
    private @FXML Button settingsCancelButton;
    private @FXML Button settingsSaveButton;

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

        // Wire cancel and save buttons
        settingsCancelButton.setOnAction(event -> SettingsController.this.stage.hide());
        settingsSaveButton.setOnAction(event -> {
            T settings = getSettings();
            for (EventHandler<T> handler : eventHandlers) {
                handler.onChange(settings);
            }
            SettingsController.this.stage.hide();
        });
    }

    // Class declarations
    private final HashSet<EventHandler<T>> eventHandlers;

    protected SettingsController() {
        // Some setup
        eventHandlers = new HashSet<>();
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
     * Returns the settings set by the user on the interface.
     */
    @Nonnull
    protected abstract T getSettings();

    /**
     * Resets the GUI to its default settings.
     */
    protected abstract void reset();

    /**
     * Resets the GUI to the given settings.
     * @param settings The settings to reset to.
     */
    protected abstract void reset(T settings);
}
