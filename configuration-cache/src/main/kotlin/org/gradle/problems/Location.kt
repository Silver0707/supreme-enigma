package org.gradle.problems

import org.gradle.api.Describable
import org.gradle.internal.DisplayName

/**
 * Identifies a position in a Gradle build script or plugin source file.
 *
 * A [Location] bundles two complementary names for the same source together with a file path and
 * line number:
 *
 * - [sourceLongDisplayName] – the full, sentence-start–capable name (e.g.
 *   *"Build file '/path/to/build.gradle'"*).  It must be a [DisplayName] because
 *   [toString] capitalizes it via [DisplayName.capitalizedDisplayName].
 * - [sourceShortDisplayName] – a concise, inline name suitable for diagnostic messages
 *   (e.g. *"build file '/path/to/build.gradle'"*).  Typed as the broader [Describable]
 *   so that any describable source – not only full [DisplayName] instances – can be used
 *   here, matching the contract of [org.gradle.internal.configuration.problems.PropertyTrace.BuildLogic.source].
 *
 * @property sourceShortDisplayName A concise, inline [Describable] identifying the source file.
 * @property filePath The absolute or project-relative path of the source file.
 * @property lineNumber The 1-based line number within [filePath].
 */
class Location(
    private val sourceLongDisplayName: DisplayName,
    val sourceShortDisplayName: Describable,
    val filePath: String,
    val lineNumber: Int
) {
    override fun toString(): String =
        "${sourceLongDisplayName.capitalizedDisplayName}: line $lineNumber"
}
