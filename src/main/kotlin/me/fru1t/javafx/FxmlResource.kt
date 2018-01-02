package me.fru1t.javafx

/**
 * Denotes a [Controller]'s FXML resource file. This is required for all Controllers as it
 * allows the [Controller.create] to create instances of the layout.
 * @constructor [value] should be the resource's path. Starting the path with `/` will point point
 * to the root of the `resources` folder.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
annotation class FxmlResource(val value: String)
