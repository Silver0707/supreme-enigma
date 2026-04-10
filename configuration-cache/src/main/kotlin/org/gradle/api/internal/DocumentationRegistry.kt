package org.gradle.api.internal

/**
 * Provides URLs to the Gradle documentation for specific pages and anchors.
 *
 * Used throughout the configuration-cache and problems infrastructure to attach
 * actionable documentation links to reported problems so users can quickly find
 * the relevant reference material.
 */
class DocumentationRegistry {
    /**
     * Returns the full URL to a documentation page section.
     *
     * @param page The documentation page slug (e.g. `"configuration_cache_status"`).
     * @param anchor The in-page anchor/fragment (e.g. `"config_cache:not_yet_implemented"`).
     * @return A fully-qualified `https://docs.gradle.org/` URL pointing to the given page and anchor.
     */
    fun getDocumentationFor(page: String, anchor: String): String =
        "https://docs.gradle.org/$page#$anchor"
}
