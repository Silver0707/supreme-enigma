package org.gradle.internal.code

import org.gradle.api.Describable
import java.net.URI

interface UserCodeSource {
    val displayName: Describable

    class Script(override val displayName: Describable, private val uri: URI?) : UserCodeSource {
        fun getUri(): URI? = uri
    }
}
