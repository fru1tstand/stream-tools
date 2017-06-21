package me.fru1t.streamtools.javafx;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Required for any Settings object. This annotation provides the default settings object to the
 * SettingsController.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface DefaultSettings {
    /**
     * The name of the field containing the default settings object.
     */
    String value();
}
