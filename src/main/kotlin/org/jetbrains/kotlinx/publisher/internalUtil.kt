package org.jetbrains.kotlinx.publisher

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.publish.maven.MavenPom
import java.util.BitSet

internal const val PUBLISHING_GROUP = "publishing"
internal typealias PomConfigurator = Action<in MavenPom>

internal fun Project.hasCorrectRootProject(): Boolean =
    rootProject.getPublicationsExtension() != null

internal fun Project.getPublicationsExtension(): PublicationsExtension? =
    extensions.findByName(PublicationsExtension.NAME) as? PublicationsExtension

internal fun Project.thisWithParents(): Sequence<Project> = generateSequence(this) { it.parent }

internal fun <T : Any> Project.getClosestProperty(getter: PublicationsExtension.() -> T?): T? {
    return thisWithParents().mapNotNull { it.getPublicationsExtension()?.getter() }.firstOrNull()
}

internal fun <T : Any> Project.getClosestDefinedProperty(getter: PublicationsExtension.() -> Property<T>): T? {
    return thisWithParents().mapNotNull { it.getPublicationsExtension()?.getter() }.firstOrNull { it.isPresent }?.orNull
}

internal fun Project.afterAllParentsEvaluate(action: () -> Unit) {
    val projects = thisWithParents().toList()
    val projectsCount = projects.size
    val evaluated = BitSet(projectsCount)

    projects.forEachIndexed { i, project ->
        fun applyAction() {
            evaluated.set(i)
            if (evaluated.cardinality() == projectsCount) {
                action()
            }
        }
        if (project.state.executed) {
            applyAction()
        } else {
            project.afterEvaluate {
                applyAction()
            }
        }
    }
}

internal fun getPublishTaskName(repoName: String, publicationName: String? = null): String {
    return if (publicationName == null) {
        "publishAllPublicationsTo${repoName.capitalized()}Repository"
    } else {
        "publish${publicationName.capitalized()}PublicationTo${repoName.capitalized()}Repository"
    }
}

internal val Project.defaultGroup: String get() {
    return project.getClosestDefinedProperty { defaultGroup } ?: project.group.toString()
}

internal fun String.capitalized() = replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
