package org.gradle.api

/**
 * Something that has a human-readable display name.
 *
 * This is the base interface for anything that can describe itself via a plain display name string.
 * It is intentionally broad so that build-logic sources, tasks, plugins, and other Gradle model
 * objects can all be treated uniformly wherever only a display name is required.
 *
 * @see org.gradle.internal.DisplayName for the narrower interface that additionally provides a capitalized form.
 */
interface Describable {
    /**
     * Returns a human-readable display name for this object.
     *
     * The returned string is suitable for use in diagnostic messages, progress logging, and
     * problem reports. It is not guaranteed to be capitalized or unique.
     */
    val displayName: String
}
