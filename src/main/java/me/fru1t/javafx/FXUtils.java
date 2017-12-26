package me.fru1t.javafx;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import me.fru1t.streamtools.controller.settings.GraphStatsSettings;

import javax.annotation.Nonnull;

/**
 * Provides shared functionality methods.
 */
public class FXUtils {
    /**
     * Determines relationship between a slider-textfield binding.
     */
    public enum SliderTextFieldType {
        INTEGER, DECIMAL
    }

    public enum FieldType {
        INTEGER("0"), DECIMAL("0");

        public final @Nonnull String defaultValue;
        FieldType(@Nonnull String defaultValue) {
            this.defaultValue = defaultValue;
        }
    }

    private static final int DEFAULT_FONT_SIZE = 16;

    /**
     * Converts a JavaFX Color to a css-friendly Hex string in the format of "#RRGGBB".
     * @param color The JavaFX Color to convert.
     * @return A css-friendly hex color code.
     */
    @Nonnull
    public static String colorToHex(@Nonnull Color color) {
        return String.format("#%02X%02X%02X",
                (int) ( color.getRed() * 255 ),
                (int) ( color.getGreen() * 255 ),
                (int) ( color.getBlue() * 255 ));
    }

    /**
     * Binds a Slider and TextField together so that when the slider is changed, the textfield is
     * updated also, and vis versa. If the user enters invalid input to the textField (anything
     * not a double), the textField will revert to the previously valid value.
     * @param slider The slider to bind.
     * @param textField The textField to bind.
     */
    public static void bindSliderToTextField(Slider slider, TextField textField,
            SliderTextFieldType type) {
        slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            // If the textField is in focus, it means the textField is changing the slider value,
            // so we don't want to cause a feedback loop.
            if (!textField.isFocused()) {
                textField.setText(((type == SliderTextFieldType.INTEGER)
                        ? newValue.intValue() : newValue) + "");
            }
        });
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!slider.isFocused()) {
                try {
                    slider.setValue(Double.valueOf(newValue));
                } catch (NumberFormatException e) {
                    textField.setText(oldValue);
                }
            }
        });
    }

    /**
     * Binds a TextField to a given type so that if the textField is given invalid input
     * (determined by the type), the textField will revert back to the old, valid value.
     * @param textField The textField to bind.
     * @param type The type that the textField should enforce.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored") // Value is ignored for the Exception handling.
    public static void bindTextFieldToType(FieldType type, TextField... textFields) {
        for (TextField textField : textFields) {
            textField.textProperty().addListener((observable, oldValue, newValue) -> {
                try {
                    switch (type) {
                        case INTEGER:
                            Integer.valueOf(newValue);
                            break;
                        case DECIMAL:
                            Double.valueOf(newValue);
                            break;
                    }
                } catch (NumberFormatException e) {
                    textField.setText(type.defaultValue);
                }
            });
        }
    }

    /**
     * Binds a checkbox to control the disable behavior of one or more nodes.
     * @param checkBox The controller checkbox.
     * @param enableOnSelected True if the nodes should be enabled when the checkbox is checked,
     * false if the nodes should be enabled when the checkbox is not selected.
     * @param nodes The nodes that the checkbox controls.
     */
    public static void bindCheckBoxToDisableNode(CheckBox checkBox, boolean enableOnSelected,
            Node... nodes) {
        checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            for (Node node : nodes) {
                // The logic table of this function is an xnor where true-true or false-false
                // results in false (don't disable), whereas true-false or false-true results in
                // true (to disable).
                node.setDisable(!(enableOnSelected == newValue));
            }
        });
    }

    public static <T> T getTextFieldValue(TextField textField, T defaultValue) {

    }
}
