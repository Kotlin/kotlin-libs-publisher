package org.jetbrains.kotlinx.publisher

import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Task

fun ArtifactPublication.composeOfTaskOutputs(tasks: List<NamedDomainObjectProvider<out Task>>) {
    composeOf {
        tasks.forEach { task -> artifact(task) }
    }
}
