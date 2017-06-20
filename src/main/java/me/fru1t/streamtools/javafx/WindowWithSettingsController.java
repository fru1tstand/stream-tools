package me.fru1t.streamtools.javafx;

import me.fru1t.javafx.Controller;
import me.fru1t.streamtools.Settings;

import java.lang.reflect.ParameterizedType;

/**
 * A specialized controller type that contains another controller that represents the settings
 * window.
 * @param <S> The settings type.
 * @param <T> The settings controller type.
 */
public abstract class WindowWithSettingsController<S extends Settings, T extends SettingsController<S>>
        extends Controller
        implements SettingsController.EventHandler<S> {

    private final SettingsController<S> settingsController;

    protected WindowWithSettingsController() {
        // Grab the class WindowWithSettingsController<T>
        ParameterizedType windowWithSettingsControllerClass =
                (ParameterizedType) getClass().getGenericSuperclass();

        // Get <T>'s class by casting the Type object from the generic <T> parameter
        @SuppressWarnings("unchecked")
        Class<T> settingsControllerClass = (Class<T>)
                windowWithSettingsControllerClass.getActualTypeArguments()[1];

        settingsController = Controller.createWithNewStage(settingsControllerClass);
        settingsController.addEventHandler(this);
    }

    /**
     * Shows the settings window.
     */
    public void showSettings() {
        settingsController.show();
    }
}
