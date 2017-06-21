package me.fru1t.streamtools.javafx;

import javafx.stage.Stage;
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

        settingsController = Controller.createWithNewStage(settingsControllerClass);
        settingsController.addEventHandler(this);
    }

    @Override
    public void setTitle(String title) {
        super.setTitle(title);
        settingsController.setTitle(title);
    }

    @Override
    public void provideStage(Stage stage) {
        super.provideStage(stage);

        // Set up scene resize listeners
        stage.widthProperty().addListener((observable, oldValue, newValue) -> {
            settingsController.currentSettings.setWindowWidth(newValue.doubleValue());
            onSettingsChange(settingsController.currentSettings);
        });
        stage.heightProperty().addListener((observable, oldValue, newValue) -> {
            settingsController.currentSettings.setWindowHeight(newValue.doubleValue());
            onSettingsChange(settingsController.currentSettings);
        });

        // Update stage
        onSettingsChange(settingsController.getCurrentSettings());
    }

    /**
     * Shows this window's settings panel, or does nothing if it's already open.
     */
    public void showSettings() {
        settingsController.show();
    }
}
