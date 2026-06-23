package org.gradle.problems

import org.gradle.internal.DisplayName

class Location(
    private val sourceLongDisplayName: DisplayName,
    val sourceShortDisplayName: DisplayName,
    val filePath: String,
    val lineNumber: Int
) {
    override fun toString(): String =
        "${sourceLongDisplayName.capitalizedDisplayName}: line $lineNumber"
}
