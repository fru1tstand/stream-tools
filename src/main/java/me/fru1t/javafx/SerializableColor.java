package me.fru1t.javafx;

import javafx.scene.paint.Color;
import lombok.Getter;

import javax.annotation.Nonnull;

/**
 * Represents a serializable version of the JavaFX Color class.
 */
public class SerializableColor {
    private transient Color color;
    private @Getter @Nonnull String colorHex = "#000";

    /**
     * Creates a new serializable color from the given JavaFX Color.
     */
    public SerializableColor(Color color) {
        setColor(color);
    }

    /**
     * Creates a new serializable color from the given string web color.
     */
    public SerializableColor(@Nonnull String color) {
        setColor(color);
    }

    public void setColor(Color color) {
        colorHex = FXUtils.colorToHex(color);
        this.color = color;
    }

    /**
     * @param color A web representation of a color.
     * @see Color#web(String)
     */
    public void setColor(String color) {
        colorHex = color;
        this.color = Color.web(color);
    }

    public Color getColor() {
        if (color == null) {
            color = Color.web(colorHex);
        }
        return color;
    }

    @Override
    public String toString() {
        return colorHex;
    }
}
