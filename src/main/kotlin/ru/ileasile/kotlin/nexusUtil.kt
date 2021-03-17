package ru.ileasile.kotlin

import io.github.gradlenexus.publishplugin.NexusPublishExtension
import io.github.gradlenexus.publishplugin.NexusPublishPlugin
import org.gradle.api.Project

private fun Project.nexusPublishing(action: (NexusPublishExtension).() -> Unit) {
    extensions.configure("nexusPublishing", action)
}

fun Project.applyNexusPlugin(settings: SonatypeSettings, packageGroup: String?) {
    pluginManager.run {
        apply(NexusPublishPlugin::class.java)
    }

    nexusPublishing {
        this.packageGroup.set(packageGroup)
        repositoryDescription.set(settings.repositoryDescription)

        repositories {
            sonatype {
                username.set(settings.username)
                password.set(settings.password)
            }
        }
    }
}
