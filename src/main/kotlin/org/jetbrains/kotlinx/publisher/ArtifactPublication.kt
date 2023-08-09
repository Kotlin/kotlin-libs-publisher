package org.jetbrains.kotlinx.publisher

import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.property

class ArtifactPublication(project: Project) {
    val publicationName: Property<String> = project.objects.property<String>().apply {
        set(project.provider { project.name })
    }
    val artifactId: Property<String> = project.objects.property<String>().apply {
        set(project.provider { project.getClosestDefinedProperty { defaultArtifactIdPrefix }.orEmpty() + publicationName.get() })
    }
    val groupId: Property<String> = project.objects.property<String>().apply {
        set(project.provider { project.defaultGroup })
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

    private var _artifactConfigureAction : ArtifactConfigurator.() -> Unit = {
        from(project.components["java"])
    }

    fun composeOf(action: ArtifactConfigurator.() -> Unit) {
        _artifactConfigureAction = action
    }

    internal fun configurePublication(publication: MavenPublication) {
        publication.artifactId = artifactId.get()
        publication.groupId = groupId.get()

        ArtifactConfigurator(publication)._artifactConfigureAction()

        publication.pom {
            name.set(packageName)
            description.set(this@ArtifactPublication.description)
        }
    }
}
