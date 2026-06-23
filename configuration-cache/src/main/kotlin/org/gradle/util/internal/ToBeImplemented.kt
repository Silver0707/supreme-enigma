package org.gradle.util.internal

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class ToBeImplemented(val value: String = "")
