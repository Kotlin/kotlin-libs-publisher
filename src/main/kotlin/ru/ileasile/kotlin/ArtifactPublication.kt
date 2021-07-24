package ru.ileasile.kotlin

import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property

class ArtifactPublication(project: Project) {
    val publicationName: Property<String> = project.objects.property<String>().apply {
        set(project.provider { project.name })
    }
    val artifactId: Property<String> = project.objects.property<String>().apply {
        set(project.provider { project.getClosestDefinedProperty { defaultArtifactIdPrefix }.orEmpty() + publicationName.get() })
    }
    val groupId: Property<String> = project.objects.property<String>().apply {
        set(project.provider { project.getClosestDefinedProperty { defaultGroup }!! })
    }
    val packageName: Property<String> = project.objects.property<String>().apply {
        set(artifactId)
    }
    val description: Property<String> = project.objects.property<String>().apply {
        set("No description provided")
    }
    val publishToSonatype: Property<Boolean> = project.objects.property<Boolean>().apply {
        set(true)
    }
}
