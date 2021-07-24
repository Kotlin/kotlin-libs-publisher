package ru.ileasile.kotlin

import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property

interface ArtifactPublication {
    val publicationName: Property<String>
    val artifactId: Property<String>
    val groupId: Property<String>
    val packageName: Property<String>
    val description: Property<String>
    val publishToSonatype: Property<Boolean>
}

internal class ArtifactPublicationImpl(objects: ObjectFactory) : ArtifactPublication {
    override val publicationName: Property<String> = objects.property()
    override val artifactId: Property<String> = objects.property()
    override val groupId: Property<String> = objects.property()
    override val packageName: Property<String> = objects.property()
    override val description: Property<String> = objects.property()
    override val publishToSonatype: Property<Boolean> = objects.property()
}

internal class DeterminedArtifactPublication(project: Project, publication: ArtifactPublication) {
    val publicationName: String = publication.publicationName.getOrElse(project.name)
    val artifactId: String = publication.artifactId.orNull ?: project.getClosestDefinedProperty { defaultArtifactIdPrefix }.orEmpty() + publicationName
    val groupId: String = publication.groupId.orNull ?: project.getClosestDefinedProperty { packageGroup }!!
    val packageName: String = publication.packageName.getOrElse(artifactId)
    val description: String = publication.description.get()
    val publishToSonatype: Boolean = publication.publishToSonatype.getOrElse(true)
}
