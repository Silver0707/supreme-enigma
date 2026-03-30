package org.gradle.internal

import org.gradle.api.Describable

interface DisplayName : Describable {
    val capitalizedDisplayName: String
}
