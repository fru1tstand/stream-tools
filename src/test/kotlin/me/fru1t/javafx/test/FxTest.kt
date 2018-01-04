package me.fru1t.javafx.test

/**
 * Marks a method as an FxTest for [FxApplicationTest] to pick up and run. Tests may run
 * asynchronously so each method annotated with [FxTest] must instantiate its own test objects.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class FxTest
