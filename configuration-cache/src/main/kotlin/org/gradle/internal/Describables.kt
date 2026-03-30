package org.gradle.internal

class Describables {
    companion object {
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
