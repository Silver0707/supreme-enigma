package org.gradle.api.internal

class DocumentationRegistry {
    fun getDocumentationFor(page: String, anchor: String): String =
        "https://docs.gradle.org/$page#$anchor"
}
