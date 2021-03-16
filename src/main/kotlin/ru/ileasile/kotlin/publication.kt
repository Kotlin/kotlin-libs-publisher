package ru.ileasile.kotlin

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.invoke
import org.gradle.plugins.signing.SigningExtension
import org.jetbrains.dokka.gradle.DokkaTask
import java.io.File
import java.nio.file.Path

private typealias PomConfigurator = Action<in MavenPom>

data class SigningCredentials(
    val key: String,
    val privateKey: String,
    val keyPassphrase: String
)

data class SonatypeSettings(
    val username: String?,
    val password: String?,
    val repositoryDescription: String
)

class PublicationsExtension(private val project: Project) {
    private var _pomConfigurator: PomConfigurator? = null
    fun pom(): PomConfigurator? = _pomConfigurator
    fun pom(configurator: PomConfigurator) {
        _pomConfigurator = configurator
    }

    private var _signingCredentials: SigningCredentials? = null
    fun signingCredentials(): SigningCredentials? = _signingCredentials
    fun signingCredentials(key: String?, privateKey: String?, keyPassphrase: String?) {
        if (key == null || privateKey == null || keyPassphrase == null) return
        _signingCredentials = SigningCredentials(key, privateKey, keyPassphrase)
    }

    private var _sonatypeSettings: SonatypeSettings? = null
    fun sonatypeSettings(): SonatypeSettings? = _sonatypeSettings

    /**
     * Apply only on root project
     */
    fun sonatypeSettings(
        username: String?,
        password: String?,
        repositoryDescription: String
    ) {
        val settings = SonatypeSettings(username, password, repositoryDescription)
        _sonatypeSettings = settings
        project.applyNexusPlugin(settings, packageGroup)

        project.tasks.named("closeRepository") {
            mustRunAfter("publishToSonatype")
        }
    }

    var packageGroup: String? = null

    fun add(publication: ArtifactPublication) {
        project.addPublication(publication)
    }

    fun add(configuration: Action<in ArtifactPublication>) {
        val res = ArtifactPublication()
        configuration(res)
        add(res)
    }

    companion object {
        const val NAME = "kotlinPublications"
    }
}

private fun Project.getPublicationsExtension(): PublicationsExtension? =
    extensions.findByName(PublicationsExtension.NAME) as? PublicationsExtension

private fun Project.parentProjects(): Sequence<Project> = generateSequence(this) { it.parent }

private fun Project.applyPomConfigurators(pom: MavenPom): MavenPom {
    return parentProjects().mapNotNull {
        it.getPublicationsExtension()?.pom()
    }.toList().foldRight(pom) { configurator, acc ->
        configurator(acc)
        acc
    }
}

private fun <T: Any> Project.getFirstExt(getter: PublicationsExtension.() -> T?): T? {
    return parentProjects().mapNotNull { it.getPublicationsExtension()?.getter() }.firstOrNull()
}

private fun Project.addPublication(settings: ArtifactPublication) {
    if (rootProject.findProperty("isMainProject") != true) return

    val sourceSets = extensions.getByName("sourceSets") as SourceSetContainer
    val mainSourceSet = sourceSets.named("main").get()
    val publicationName = settings.publicationName!!
    val sonatypeSettings = project.getFirstExt { sonatypeSettings() }

    tasks {
        register("sourceJar", Jar::class.java) {
            archiveClassifier.set("sources")

            from(mainSourceSet.allSource)
        }

        named("dokkaHtml", DokkaTask::class.java) {
            outputDirectory.set(File("$buildDir/dokka"))
        }

        val javadocDestDir = File("$buildDir/dokkaJavadoc")

        val dokkaJavadoc = named("dokkaJavadoc", DokkaTask::class.java) {
            outputDirectory.set(javadocDestDir)
            inputs.dir("src/main/kotlin")
        }

        @Suppress("UNUSED_VARIABLE")
        register("javadocJar", Jar::class.java) {
            group = "documentation"
            dependsOn(dokkaJavadoc)
            archiveClassifier.set("javadoc")
            from(javadocDestDir)
        }
    }

    extensions.configure<PublishingExtension>("publishing") {
        publications {
            register(publicationName, MavenPublication::class.java) {
                artifactId = settings.artifactId
                groupId = settings.groupId ?: getFirstExt { packageGroup }

                from(components["java"])
                artifact(tasks["sourceJar"])
                artifact(tasks["javadocJar"])

                pom {
                    name.set(settings.packageName)
                    description.set(settings.description)

                    applyPomConfigurators(this)
                }
            }
        }

        repositories {
            (rootProject.findProperty("localPublicationsRepo") as? Path)?.let {
                maven {
                    name = "LocalBuild"
                    url = it.toUri()
                }
            }
        }
    }

    val thisProjectName = project.name
    val thisProject = project

    rootProject.tasks {
        named("publishLocal") {
            val projectPrefix = if (thisProject != rootProject) ":$thisProjectName" else ""
            dependsOn( "$projectPrefix:publishAllPublicationsToLocalBuildRepository")
        }

        if (settings.publishToSonatype && sonatypeSettings != null) {
            thisProject.configureNexusPublish(sonatypeSettings)
            if (thisProject != rootProject) {
                named("publishToSonatype") {
                    dependsOn(":$thisProjectName:publishToSonatype")
                }
            }
        }
    }

    getFirstExt { signingCredentials() }?.apply {
        extensions.configure<SigningExtension>("signing") {
            sign(extensions.getByName<PublishingExtension>("publishing").publications[publicationName])

            @Suppress("UnstableApiUsage")
            useInMemoryPgpKeys(key, privateKey, keyPassphrase)
        }
    }
}
