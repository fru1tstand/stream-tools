package me.fru1t.javafx;

import javafx.scene.control.TextInputDialog;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Utility functions for {@link TextInputDialog}
 */
public class TextInputDialogUtils {
    @Nullable
    public static String createShowAndWait(@Nullable String title, @Nullable String headerText,
            @Nullable String contentText) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(headerText);
        dialog.setContentText(contentText);
        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }
}
