package org.gradle.internal.cc.impl.problems

enum class ProblemSeverity {

    /**
     * Problems that are reported to the user sometime after they are discovered,
     * but which will fail the build, unless warning-mode is active.
     */
    Deferred,

    /**
     * Problems that interrupt the current operation immediately after being discovered and recorded.
     */
    Interrupting,

    /**
     * A problem produced by a task marked as notCompatibleWithConfigurationCache.
     */
    Suppressed,

    /**
     * A problem produced by a task that requested Configuration Cache degradation.
     */
    SuppressedSilently
}
