package org.gradle.internal.code

import org.gradle.api.Describable
import java.net.URI

/**
 * Represents the source of user-provided Gradle code, such as a build script or a plugin class.
 *
 * Implementations identify where a piece of configuration logic originates so that
 * [org.gradle.internal.configuration.problems.PropertyTrace.BuildLogic] can report meaningful
 * diagnostics when configuration-cache problems are detected.
 */
interface UserCodeSource {
    /** A [Describable] that provides a human-readable name for this source. */
    val displayName: Describable

    /**
     * A [UserCodeSource] backed by a script file, optionally associated with a [URI].
     *
     * @property displayName A [Describable] identifying the script (e.g. its file path).
     */
    class Script(override val displayName: Describable, private val uri: URI?) : UserCodeSource {
        /** Returns the [URI] of the script file, or `null` if no URI is available. */
        fun getUri(): URI? = uri
    }
}
