package org.gradle.internal.logging

import java.io.File

class ConsoleRenderer {
    fun asClickableFileUrl(file: File): String = file.toURI().toString()
}
