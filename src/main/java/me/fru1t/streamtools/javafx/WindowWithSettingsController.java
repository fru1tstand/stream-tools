package me.fru1t.streamtools.javafx;

import javafx.beans.value.ChangeListener;
import me.fru1t.javafx.Controller;
import me.fru1t.streamtools.Settings;

import java.lang.reflect.ParameterizedType;

/**
 * A specialized controller type that contains another controller that represents the settings
 * window.
 * @param <S> The settings type.
 * @param <T> The settings controller type.
 */
public abstract class WindowWithSettingsController<S extends Settings<S>, T extends SettingsController<S>>
        extends Controller
        implements SettingsController.EventHandler<S> {

    private final SettingsController<S> settingsController;
    private final ChangeListener<Number> sizeChangeListener;

    protected WindowWithSettingsController() {
        // This constructor grabs the type T from whatever class extended
        // WindowWithSettingsController. It then creates an instance of that SettingsController.

        // Grab the class WindowWithSettingsController<T>
        ParameterizedType windowWithSettingsControllerClass =
                (ParameterizedType) getClass().getGenericSuperclass();

        // Get <T>'s class by casting the Type object from the generic <T> parameter
        @SuppressWarnings("unchecked")
        Class<T> settingsControllerClass = (Class<T>)
                windowWithSettingsControllerClass.getActualTypeArguments()[1];

        settingsController = Controller.Companion.createWithNewStage(settingsControllerClass);
        settingsController.addEventHandler(this);

        sizeChangeListener =
            (observable, oldValue, newValue)
                -> onSettingsChange(settingsController.getCurrentSettings());
    }

    @Override
    public void shutdown() {
        settingsController.removeEventHandler(this);
        settingsController.shutdown();
        super.shutdown();
    }

    @Override
    public void show() {
        if (getStage() != null) {
            getStage().widthProperty().removeListener(sizeChangeListener);
            getStage().heightProperty().removeListener(sizeChangeListener);
            getStage().widthProperty().addListener(sizeChangeListener);
            getStage().heightProperty().addListener(sizeChangeListener);
        }

        onSettingsChange(settingsController.getCurrentSettings());
        super.show();
    }

    /**
     * @return The settings controller for this window.
     */
    public SettingsController<S> getSettingsController() {
        return settingsController;
    }
}
