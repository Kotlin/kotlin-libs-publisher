package ru.ileasile.kotlin

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.publish.maven.MavenPom
import org.gradle.kotlin.dsl.invoke

internal const val PUBLISHING_GROUP = "publishing"
internal typealias PomConfigurator = Action<in MavenPom>

internal fun Project.hasCorrectRootProject(): Boolean =
    rootProject.getPublicationsExtension() != null

internal fun Project.getPublicationsExtension(): PublicationsExtension? =
    extensions.findByName(PublicationsExtension.NAME) as? PublicationsExtension

internal fun Project.thisWithParents(): Sequence<Project> = generateSequence(this) { it.parent }

internal fun Project.applyPomConfigurators(pom: MavenPom): MavenPom {
    return thisWithParents().mapNotNull {
        it.getPublicationsExtension()?.pom()
    }.toList().foldRight(pom) { configurator, acc ->
        configurator(acc)
        acc
    }
}

internal fun <T : Any> Project.getClosestProperty(getter: PublicationsExtension.() -> T?): T? {
    return thisWithParents().mapNotNull { it.getPublicationsExtension()?.getter() }.firstOrNull()
}

internal fun <T : Any> Project.getClosestDefinedProperty(getter: PublicationsExtension.() -> Property<T>): T? {
    return thisWithParents().mapNotNull { it.getPublicationsExtension()?.getter() }.firstOrNull { it.isPresent }?.orNull
}
