package org.gradle.internal

/**
 * Factory for creating [DisplayName] instances from arbitrary objects.
 *
 * Provides a single entry-point, [of], that converts any value to a [DisplayName] by calling
 * [Any.toString] and capitalizing the result on demand.
 */
class Describables {
    companion object {
        /**
         * Wraps [name] in a [DisplayName] whose [DisplayName.displayName] is `name.toString()`.
         *
         * The [DisplayName.capitalizedDisplayName] property converts the first character to
         * title-case, making the result suitable for sentence-start contexts.
         *
         * @param name The value to wrap; its [Any.toString] result is used as the display name.
         * @return A [DisplayName] backed by `name.toString()`.
         */
        @JvmStatic
        fun of(name: Any): DisplayName = SimpleDisplayName(name.toString())
    }
}

private class SimpleDisplayName(private val name: String) : DisplayName {
    override val displayName: String get() = name
    override val capitalizedDisplayName: String
        get() = name.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    override fun toString(): String = name
    override fun equals(other: Any?): Boolean = other is SimpleDisplayName && name == other.name
    override fun hashCode(): Int = name.hashCode()
}
