package me.fru1t.javafx;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Denotes a {@link Controller}'s FXML resource file. This is required for all Controllers as it
 * allows the {@link Controller#create(Class)} to create instances of the layout.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface FXMLResource {
    /**
     * The relative path (from the application's classpath) to this controller's FXML resource
     * file.
     */
    String value();
}
