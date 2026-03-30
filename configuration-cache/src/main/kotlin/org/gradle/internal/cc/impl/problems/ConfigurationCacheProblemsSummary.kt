package org.gradle.internal.cc.impl.problems

import com.google.common.annotations.VisibleForTesting
import com.google.common.collect.Comparators
import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap
import org.gradle.api.internal.DocumentationRegistry
import org.gradle.internal.configuration.problems.DocumentationSection
import org.gradle.internal.configuration.problems.PropertyProblem
import org.gradle.internal.extensions.stdlib.capitalized
import org.gradle.internal.logging.ConsoleRenderer
import java.io.File
import java.util.Comparator.comparing
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock


@VisibleForTesting
internal const val MAX_CONSOLE_PROBLEMS = 15


private const val MAX_PROBLEM_EXCEPTIONS = 5

/**
 * Collects problems and produces a summary upon request.
 *
 * The primary use case for the summary is to collect information to be presented on the console.
 *
 * <h2>Problem uniqueness</h2>
 *
 * For the console, a problem is unique if there are no other problems with same message, top location frame and documentation link.
 *
 * However, for the report, problem uniqueness also takes the full location stack into account.
 *
 * <h2>Thread safety</h2>
 *
 * This class is thread-safe.
 */
internal class ConfigurationCacheProblemsSummary(

    /**
     * The maximum number of unique problems that should be collected.
     */
    private val maxCollectedProblems: Int = 4096

) {
    private var totalProblemCount: Int = 0

    private var deferredProblemCount: Int = 0

    private var suppressedProblemCount: Int = 0

    private var suppressedSilentlyProblemCount: Int = 0

    private var overflownProblemCount: Int = 0

    private var incompatibleTasksCount: Int = 0

    private var incompatibleFeatureCount: Int = 0

    private val problemCauses = HashMap<ProblemCause, ProblemSeverity>()

    private val originalProblemExceptions = ArrayList<Throwable>(MAX_PROBLEM_EXCEPTIONS)

    private val severityComparator = consoleComparatorForSeverity()

    private val lock = ReentrantLock()

    fun get(): Summary = lock.withLock {
        Summary(
            totalProblemCount = totalProblemCount,
            reportUniqueProblemCount = problemCauses.size,
            deferredProblemCount = deferredProblemCount,
            consoleProblemCount = totalProblemCount - suppressedSilentlyProblemCount,
            overflownProblemCount = overflownProblemCount,
            consoleProblemCauses = problemCausesForConsole(),
            originalProblemExceptions = ImmutableList.copyOf(originalProblemExceptions),
            maxCollectedProblems = maxCollectedProblems,
            incompatibleTasksCount = incompatibleTasksCount,
            incompatibleFeatureCount = incompatibleFeatureCount
        )
    }

    private fun problemCausesForConsole(): ImmutableMap<ProblemCause, ProblemSeverity> =
        ImmutableMap.copyOf(
            problemCauses.filterValues { it != ProblemSeverity.SuppressedSilently }
                .mapKeys { (k, _) -> k.asShallow() }
        )

    /**
     * Returns `true` if the problem was accepted, `false` if it was rejected because the maximum number of unique problems was reached,
     * or the problem was not report-unique.
     */
    fun onProblem(problem: PropertyProblem, severity: ProblemSeverity): Boolean {
        lock.withLock {
            totalProblemCount += 1
            when (severity) {
                ProblemSeverity.Deferred -> deferredProblemCount += 1
                ProblemSeverity.Suppressed -> suppressedProblemCount += 1
                ProblemSeverity.SuppressedSilently -> suppressedSilentlyProblemCount += 1
                ProblemSeverity.Interrupting -> {}
            }
            if (problemCauses.size == maxCollectedProblems) {
                overflownProblemCount += 1
                return false
            }
            val isNewCause = recordProblemCause(problem, severity)
            if (!isNewCause) {
                return false
            }
            if (severity != ProblemSeverity.Interrupting) {
                collectOriginalException(problem)
            }
            return true
        }
    }

    fun onIncompatibleTask() {
        lock.withLock {
            incompatibleTasksCount += 1
        }
    }

    fun onIncompatibleFeature(problem: PropertyProblem) {
        lock.withLock {
            onProblem(problem, ProblemSeverity.SuppressedSilently)
            incompatibleFeatureCount += 1
        }
    }

    private fun collectOriginalException(problem: PropertyProblem) {
        if (originalProblemExceptions.size < MAX_PROBLEM_EXCEPTIONS) {
            problem.exception?.let {
                originalProblemExceptions.add(it)
            }
        }
    }

    private fun recordProblemCause(problem: PropertyProblem, severity: ProblemSeverity): Boolean {
        val newCause = ProblemCause.of(problem)
        var problemAccepted = true
        problemCauses.merge(newCause, severity) { existingSeverity, newSeverity ->
            val newSeverityIsHigher = severityComparator.compare(existingSeverity, newSeverity) > 0
            if (newSeverityIsHigher) {
                newSeverity
            } else {
                problemAccepted = false
                existingSeverity
            }
        }
        return problemAccepted
    }
}


internal class Summary(
    val totalProblemCount: Int,

    val consoleProblemCount: Int,

    val deferredProblemCount: Int,

    private val consoleProblemCauses: Map<ProblemCause, ProblemSeverity>,

    val reportUniqueProblemCount: Int,

    val overflownProblemCount: Int,

    val originalProblemExceptions: List<Throwable>,

    private val maxCollectedProblems: Int,

    private val incompatibleTasksCount: Int,

    private val incompatibleFeatureCount: Int
) {
    @VisibleForTesting
    internal val overflowed: Boolean get() = overflownProblemCount > 0

    @VisibleForTesting
    internal val consoleUniqueProblemCount = consoleProblemCauses.size

    /**
     * Builds the console feedback string for configuration cache problems.
     */
    fun textForConsole(cacheActionText: String, htmlReportFile: File? = null): String {
        val documentationRegistry = DocumentationRegistry()
        return StringBuilder().apply {
            val topConsoleProblems = topProblemsForConsole().iterator()
            val hasConsoleProblems = topConsoleProblems.hasNext()
            if (hasConsoleProblems) {
                appendLine()
                appendSummaryHeader(cacheActionText)
                appendLine()
                topConsoleProblems.forEach { problem ->
                    append("- ")
                    append(problem.userCodeLocation.capitalized())
                    append(": ")
                    appendLine(problem.message)
                    problem.documentationSection?.let {
                        appendLine("  See ${documentationRegistry.getDocumentationFor(it.page, it.anchor)}")
                    }
                }
                val notShown = consoleUniqueProblemCount - MAX_CONSOLE_PROBLEMS
                if (notShown > 0) {
                    val problemStr = if (notShown > 1) "problems" else "problem"
                    appendLine("plus $notShown more $problemStr. Please see the report for details.")
                }
            }
            val hasIncompatibleTasks = incompatibleTasksCount > 0
            val hasIncompatibleFeatures = incompatibleFeatureCount > 0
            htmlReportFile?.let {
                appendLine()
                if ((hasIncompatibleTasks || hasIncompatibleFeatures) && !hasConsoleProblems) {
                    append("Some tasks or features in this build are not compatible with the configuration cache.")
                    appendLine()
                }
                append(buildSummaryReportLink(it))
            }
        }.toString()
    }

    private fun topProblemsForConsole(): Sequence<ProblemCause> =
        consoleProblemCauses.entries.stream()
            .collect(Comparators.least(MAX_CONSOLE_PROBLEMS, consoleComparatorForProblemCauseWithSeverity()))
            .asSequence()
            .map { it.key }

    private fun StringBuilder.appendSummaryHeader(cacheAction: String) {
        append(consoleProblemCount)
        append(if (consoleProblemCount == 1) " problem was found " else " problems were found ")
        append(cacheAction)
        append(" the configuration cache")
        if (overflowed) {
            append(", only the first ")
            append(maxCollectedProblems)
            append(" were considered")
        }
        if (consoleUniqueProblemCount != consoleProblemCount) {
            append(", ")
            append(consoleUniqueProblemCount)
            append(" of which ")
            append(if (consoleUniqueProblemCount == 1) "seems unique" else "seem unique")
        }
        append(".")
    }

    private fun buildSummaryReportLink(reportFile: File) =
        "See the complete report at ${clickableUrlFor(reportFile)}"

    private fun clickableUrlFor(file: File) =
        ConsoleRenderer().asClickableFileUrl(file)

    @VisibleForTesting
    internal fun severityFor(of: ProblemCause): ProblemSeverity? =
        this.consoleProblemCauses[of.asShallow()]
}


private fun consoleComparatorForProblemCauseWithSeverity(): Comparator<Map.Entry<ProblemCause, ProblemSeverity>> =
    comparing<Map.Entry<ProblemCause, ProblemSeverity>, ProblemSeverity>({ it.value }, consoleComparatorForSeverity())
        .thenComparing({ it.key }, consoleComparatorForProblemCause())


private fun consoleComparatorForProblemCause(): Comparator<ProblemCause> =
    comparing { p: ProblemCause -> p.userCodeLocation }
        .thenComparing { p: ProblemCause -> p.message }


private fun consoleComparatorForSeverity(): Comparator<ProblemSeverity> =
    Comparator.comparingInt { it: ProblemSeverity ->
        when (it) {
            ProblemSeverity.Deferred -> 1
            ProblemSeverity.Suppressed -> 2
            ProblemSeverity.Interrupting -> 3
            ProblemSeverity.SuppressedSilently -> Int.MAX_VALUE
        }
    }


/**
 * A subset of [PropertyProblem] information used for summarization of all observed problems.
 */
internal data class ProblemCause(
    val userCodeLocation: String,
    val message: String,
    val documentationSection: DocumentationSection?,
    private val traceHash: Int?
) {
    private val isShallowTrace: Boolean get() = traceHash == null

    fun asShallow(): ProblemCause =
        if (isShallowTrace)
            this
        else
            ProblemCause(userCodeLocation, message, documentationSection, null)

    companion object {
        fun of(problem: PropertyProblem) = problem.run {
            ProblemCause(
                trace.containingUserCode,
                message.render(),
                documentationSection,
                trace.fullHash
            )
        }
    }
}
