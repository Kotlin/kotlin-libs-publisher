package org.jetbrains.kotlinx.publisher

import org.gradle.api.component.SoftwareComponent
import org.gradle.api.publish.maven.MavenArtifact
import org.gradle.api.publish.maven.MavenPublication

class ArtifactConfigurator(private val mavenPublication: MavenPublication) {
    fun from(component: SoftwareComponent) {
        mavenPublication.from(component)
    }

    fun artifact(source: Any): MavenArtifact {
        return mavenPublication.artifact(source)
    }

    fun artifact(source: Any, config: MavenArtifact.() -> Unit): MavenArtifact {
        return mavenPublication.artifact(source, config)
    }
}
