package ru.ileasile.kotlin

import de.marcphilipp.gradle.nexus.NexusPublishExtension
import de.marcphilipp.gradle.nexus.NexusPublishPlugin
import io.codearte.gradle.nexus.NexusStagingExtension
import io.codearte.gradle.nexus.NexusStagingPlugin
import org.gradle.api.Project

fun Project.configureNexusPublish(settings: SonatypeSettings) {
    extensions.configure<NexusPublishExtension>("nexusPublishing") {
        repositories {
            sonatype {
                username.set(settings.username)
                password.set(settings.password)
            }
        }
    }
}

fun Project.applyNexusPlugin(settings: SonatypeSettings, packageGroup: String?) {
    pluginManager.run {
        apply(NexusStagingPlugin::class.java)
        apply(NexusPublishPlugin::class.java)
    }

    extensions.configure<NexusStagingExtension>("nexusStaging") {
        username = settings.username
        password = settings.password
        this.packageGroup = packageGroup
        repositoryDescription = settings.repositoryDescription
    }
}
