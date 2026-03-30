package org.gradle.internal.configuration.problems

import org.gradle.api.Describable
import org.gradle.internal.code.UserCodeSource
import org.gradle.internal.configuration.problems.StructuredMessage.Fragment.Reference
import org.gradle.internal.configuration.problems.StructuredMessage.Fragment.Text
import org.gradle.problems.Location
import kotlin.reflect.KClass


/**
 * A problem that does not necessarily compromise the execution of the build.
 */
data class PropertyProblem(
    val trace: PropertyTrace,
    val message: StructuredMessage,
    val exception: Throwable? = null,
    val documentationSection: DocumentationSection? = null
)


// TODO:configuration-cache extract interface and move enum back to :configuration-cache
enum class DocumentationSection(val page: String, val anchor: String) {
    NotYetImplemented("configuration_cache_status", "config_cache:not_yet_implemented"),
    NotYetImplementedSourceDependencies("configuration_cache_status", "config_cache:not_yet_implemented:source_dependencies"),
    NotYetImplementedJavaSerialization("configuration_cache_status", "config_cache:not_yet_implemented:java_serialization"),
    NotYetImplementedTestKitJavaAgent("configuration_cache_status", "config_cache:not_yet_implemented:testkit_build_with_java_agent"),
    NotYetImplementedBuildServiceInFingerprint("configuration_cache_status", "config_cache:not_yet_implemented:build_services_in_fingerprint"),
    NotYetImplementedBuildEventListeners("configuration_cache_status", "config_cache:not_yet_implemented:more_build_event_listeners"),
    TaskOptOut("configuration_cache_debugging", "config_cache:task_opt_out"),
    RequirementsBuildListeners("configuration_cache_requirements", "config_cache:requirements:build_listeners"),
    RequirementsDisallowedTypes("configuration_cache_requirements", "config_cache:requirements:disallowed_types"),
    RequirementsExternalProcess("configuration_cache_requirements", "config_cache:requirements:external_processes"),
    RequirementsTaskAccess("configuration_cache_requirements", "config_cache:requirements:task_access"),
    RequirementsSysPropEnvVarRead("configuration_cache_requirements", "config_cache:requirements:reading_sys_props_and_env_vars"),
    RequirementsUseProjectDuringExecution("configuration_cache_requirements", "config_cache:requirements:use_project_during_execution"),
    RequirementsGradleModelTypes("configuration_cache_requirements", "config_cache:requirements:gradle_model_types"),
}


typealias StructuredMessageBuilder = StructuredMessage.Builder.() -> Unit

const val BACKTICK = '`'

private const val SINGLE_QUOTE = '\''


data class StructuredMessage(val fragments: List<Fragment>) {

    fun render(quote: Char = SINGLE_QUOTE) = fragments.joinToString(separator = "") { fragment ->
        when (fragment) {
            is Text -> fragment.text
            is Reference -> "$quote${fragment.name}$quote"
        }
    }

    override fun toString(): String = render()

    sealed class Fragment {
        data class Text(val text: String) : Fragment()
        data class Reference(val name: String) : Fragment()
    }

    companion object {
        fun forText(text: String) = StructuredMessage(listOf(Text(text)))

        fun build(builder: StructuredMessageBuilder) = StructuredMessage(
            Builder().apply(builder).fragments
        )
    }

    class Builder {
        internal val fragments = mutableListOf<Fragment>()

        fun text(string: String): Builder = apply {
            fragments.add(Text(string))
        }

        fun reference(name: String): Builder = apply {
            fragments.add(Reference(name))
        }

        fun reference(type: Class<*>): Builder = apply {
            reference(type.name)
        }

        fun reference(type: KClass<*>): Builder = apply {
            reference(type.qualifiedName!!)
        }

        fun message(message: StructuredMessage): Builder = apply {
            fragments.addAll(message.fragments)
        }

        fun build(): StructuredMessage = StructuredMessage(fragments.toList())
    }
}


/**
 * Subtypes are expected to support [PropertyTrace.equals] and [PropertyTrace.hashCode].
 */
sealed class PropertyTrace {

    object Unknown : PropertyTrace() {
        override fun toString(): String = asString()
        override fun equals(other: Any?): Boolean = other === this
        override fun hashCode(): Int = 0
        override fun describe(builder: StructuredMessage.Builder) {
            builder.text("unknown location")
        }
    }

    object Gradle : PropertyTrace() {
        override fun toString(): String = asString()
        override fun equals(other: Any?): Boolean = other === this
        override fun hashCode(): Int = 1
        override fun describe(builder: StructuredMessage.Builder) {
            builder.text("Gradle runtime")
        }
    }

    data class BuildLogic internal constructor(
        val source: Describable,
        val lineNumber: Int? = null
    ) : PropertyTrace() {
        constructor(location: Location) : this(location.sourceShortDisplayName, location.lineNumber)
        constructor(userCodeSource: UserCodeSource) : this(userCodeSource.displayName)
        override fun toString(): String = asString()
        override fun describe(builder: StructuredMessage.Builder) {
            with(builder) {
                text(source.displayName)
                lineNumber?.let {
                    text(": line $it")
                }
            }
        }
    }

    data class BuildLogicClass(
        val name: String
    ) : PropertyTrace() {
        override fun toString(): String = asString()
        override fun describe(builder: StructuredMessage.Builder) {
            with(builder) {
                text("class ")
                reference(name)
            }
        }
    }

    data class Task(
        val type: Class<*>,
        val path: String
    ) : PropertyTrace() {
        override fun toString(): String = asString()
        override fun describe(builder: StructuredMessage.Builder) {
            with(builder) {
                text("task ")
                reference(path)
                text(" of type ")
                reference(type.name)
            }
        }
    }

    data class Bean(
        val type: Class<*>,
        val trace: PropertyTrace
    ) : PropertyTrace() {
        override val containingUserCodeMessage: StructuredMessage
            get() = trace.containingUserCodeMessage
        override fun toString(): String = asString()
        override fun describe(builder: StructuredMessage.Builder) {
            with(builder) {
                reference(type.name)
                text(" bean found in ")
            }
        }
    }

    data class Property(
        val kind: PropertyKind,
        val name: String,
        val trace: PropertyTrace
    ) : PropertyTrace() {
        override val containingUserCodeMessage: StructuredMessage
            get() = trace.containingUserCodeMessage
        override fun toString(): String = asString()
        override fun describe(builder: StructuredMessage.Builder) {
            with(builder) {
                text("$kind ")
                reference(name)
                text(" of ")
            }
        }
    }

    data class Project(
        val path: String,
        val trace: PropertyTrace
    ) : PropertyTrace() {
        override val containingUserCodeMessage: StructuredMessage
            get() = trace.containingUserCodeMessage
        override fun toString(): String = asString()
        override fun describe(builder: StructuredMessage.Builder) {
            with(builder) {
                text("project ")
                reference(path)
                text(" in ")
            }
        }
    }

    data class SystemProperty(
        val name: String,
        val trace: PropertyTrace
    ) : PropertyTrace() {
        override val containingUserCodeMessage: StructuredMessage
            get() = trace.containingUserCodeMessage
        override fun toString(): String = asString()
        override fun describe(builder: StructuredMessage.Builder) {
            with(builder) {
                text("system property ")
                reference(name)
                text(" set at ")
            }
        }
    }

    data class VirtualProperty(
        val name: String,
        val owner: PropertyTrace
    ) : PropertyTrace() {
        override val containingUserCodeMessage: StructuredMessage
            get() = owner.containingUserCodeMessage
        override fun toString(): String = asString()
        override fun describe(builder: StructuredMessage.Builder) {
            with(builder) {
                text("$name of ")
            }
        }
    }

    abstract override fun equals(other: Any?): Boolean

    abstract override fun hashCode(): Int

    abstract override fun toString(): String

    protected fun asString(): String = StructuredMessage.Builder().apply {
        sequence.forEach {
            it.describe(this)
        }
    }.build().render(BACKTICK)

    fun render(): String = asString()

    open val containingUserCode: String
        get() = containingUserCodeMessage.render(BACKTICK)

    open val containingUserCodeMessage: StructuredMessage
        get() = StructuredMessage.Builder().also {
            describe(it)
        }.build()

    abstract fun describe(builder: StructuredMessage.Builder)

    val sequence: Sequence<PropertyTrace>
        get() = sequence {
            var trace = this@PropertyTrace
            while (true) {
                yield(trace)
                trace = trace.tail ?: break
            }
        }

    val fullHash: Int get() = sequence.fold(17) { acc, trace -> 31 * acc + trace.hashCode() }

    private val tail: PropertyTrace?
        get() = when (this) {
            is Bean -> trace
            is Property -> trace
            is VirtualProperty -> owner
            is SystemProperty -> trace
            is Project -> trace
            is Task -> null
            is BuildLogic -> null
            is BuildLogicClass -> null
            Gradle -> null
            Unknown -> null
        }
}


enum class PropertyKind {
    Field {
        override fun toString() = "field"
    },
    PropertyUsage {
        override fun toString() = "property usage"
    },
    InputProperty {
        override fun toString() = "input property"
    },
    OutputProperty {
        override fun toString() = "output property"
    }
}
