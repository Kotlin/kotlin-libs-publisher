package org.jetbrains.kotlinx.publisher

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.plugins.signing.SigningPlugin
import org.jetbrains.dokka.gradle.DokkaBasePlugin
import org.jetbrains.dokka.gradle.formats.DokkaHtmlPlugin
import org.jetbrains.dokka.gradle.formats.DokkaJavadocPlugin

@Suppress("unused")
class ApiPublishGradlePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.pluginManager.run {
            apply(DokkaBasePlugin::class.java)
            apply(DokkaHtmlPlugin::class.java)
            apply(DokkaJavadocPlugin::class.java)
            apply(MavenPublishPlugin::class.java)
            apply(SigningPlugin::class.java)
        }

        target.extensions.add(PublicationsExtension.NAME, PublicationsExtension(target))
    }
}
