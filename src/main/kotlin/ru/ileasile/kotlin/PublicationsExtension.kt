package ru.ileasile.kotlin

import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.Property
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.property
import org.gradle.plugins.signing.SigningExtension
import org.jetbrains.dokka.gradle.DokkaTask

class PublicationsExtension(private val project: Project) {

    init {
        project.afterEvaluate {
            bindPublications()
        }
    }

    private var _pomConfigurator: PomConfigurator? = null
    fun pom(): PomConfigurator? = _pomConfigurator

    @Suppress("unused")
    fun pom(configurator: PomConfigurator) {
        _pomConfigurator = configurator
    }

    @Suppress("unused")
    fun pom(configurator: Closure<in MavenPom>) {
        _pomConfigurator = PomConfigurator { project.configure(this, configurator) }
    }

    private var _signingCredentials: SigningCredentials? = null
    @Suppress("MemberVisibilityCanBePrivate")
    fun signingCredentials(): SigningCredentials? = _signingCredentials

    @Suppress("unused")
    fun signingCredentials(key: String?, privateKey: String?, keyPassphrase: String?) {
        if (key == null || privateKey == null || keyPassphrase == null) return
        _signingCredentials = SigningCredentials(key, privateKey, keyPassphrase)
    }

    private var _sonatypeSettings: SonatypeSettings? = null
    @Suppress("MemberVisibilityCanBePrivate")
    fun sonatypeSettings(): SonatypeSettings? = _sonatypeSettings

    /**
     * Apply only on root project
     */
    @Suppress("unused")
    fun sonatypeSettings(
        username: String?,
        password: String?,
        repositoryDescription: String
    ) {
        project.tasks.register(PUBLISH_LOCAL_TASK) {
            group = PUBLISHING_GROUP
        }

        project.tasks.register(PUBLISH_TO_SONATYPE_WITH_EXCLUDING_TASK) {
            group = PUBLISHING_GROUP
        }

        project.tasks.register(PUBLISH_TO_SONATYPE_AND_RELEASE_TASK) {
            group = PUBLISHING_GROUP

            dependsOn(PUBLISH_TO_SONATYPE_WITH_EXCLUDING_TASK, CLOSE_AND_RELEASE_TASK)
        }

        val settings = SonatypeSettings(username, password, repositoryDescription)
        _sonatypeSettings = settings

        packageGroup.set(packageGroup.orNull ?: project.group.toString())
        project.applyNexusPlugin(settings, packageGroup.get())

        project.tasks.named(CLOSE_AND_RELEASE_TASK) {
            mustRunAfter(PUBLISH_TO_SONATYPE_WITH_EXCLUDING_TASK)
        }
    }

    val packageGroup: Property<String> = project.objects.property()
    val defaultArtifactIdPrefix: Property<String> = project.objects.property()

    @Suppress("MemberVisibilityCanBePrivate")
    fun publication(publication: ArtifactPublication) {
        project.afterEvaluate {
            addPublication(publication)
        }
    }

    @Suppress("unused")
    fun publication(configuration: Action<in ArtifactPublication>) {
        val res = ArtifactPublicationImpl(project.objects)
        configuration(res)
        publication(res)
    }

    @Suppress("unused")
    fun publication(configuration: Closure<in ArtifactPublication>) {
        val res = ArtifactPublicationImpl(project.objects)
        configuration.delegate = res
        configuration.resolveStrategy = Closure.DELEGATE_FIRST
        configuration(res)
        publication(res)
    }

    private val repositoryConfigurators = mutableListOf<Action<in RepositoryHandler>>()

    @Suppress("unused")
    fun repositories(configure: Action<in RepositoryHandler>) {
        repositoryConfigurators.add(configure)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun RepositoryHandler.localRepository(name: String?, path: Any): MavenArtifactRepository {
        return maven {
            name?.let { this.name = it }
            this.url = project.file(path).toURI()
        }
    }

    @Suppress("unused")
    fun RepositoryHandler.localRepository(path: Any) = localRepository(null, path)

    @Suppress("unused")
    fun RepositoryHandler.defaultLocalRepository() = localRepository(
        "Local",
        project.rootProject.findProperty("localPublicationsRepo")
            ?: project.rootProject.buildDir.toPath().resolve("artifacts/maven")
    )

    @Suppress("MemberVisibilityCanBePrivate")
    val inheritRepositories: Property<Boolean> = project.objects.property()

    private fun applyRepositoriesWithInheritance(repositoryHandler: RepositoryHandler) {
        val extensions = if (inheritRepositories.getOrElse(true)) {
            project.thisWithParents().mapNotNull { it.getPublicationsExtension() }.toList()
        } else listOf(this)

        extensions.flatMap { it.repositoryConfigurators }.forEach {
            it.execute(repositoryHandler)
        }
    }

    private fun bindPublications() {
        if (!project.rootProject.hasCorrectRootProject()) return

        var repositoryNames = setOf<String>()

        project.extensions.configure<PublishingExtension> {
            repositoryNames = repositories.names.toSet()
            repositories {
                applyRepositoriesWithInheritance(this)
            }
            repositoryNames = repositories.names - repositoryNames
        }

        val thisProject = project

        project.rootProject.tasks {
            named(PUBLISH_LOCAL_TASK) {
                repositoryNames.forEach { repoName ->
                    dependsOn(thisProject.tasks[getPublishTaskName(repoName)])
                }
            }
        }
    }

    private fun addPublication(publication: ArtifactPublication) {
        if (!project.rootProject.hasCorrectRootProject()) return

        val determinedPublication = DeterminedArtifactPublication(project, publication)
        val publicationName = determinedPublication.publicationName

        project.extensions.configure<JavaPluginExtension> {
            withSourcesJar()
            withJavadocJar()
        }

        project.tasks {
            named<DokkaTask>(DOKKA_HTML_TASK) {
                outputDirectory.set(project.buildDir.resolve("dokkaHtml"))
            }

            val javadocDestDir = project.buildDir.resolve("dokkaJavadoc")

            val dokkaJavadoc = named<DokkaTask>(DOKKA_JAVADOC_TASK) {
                outputDirectory.set(javadocDestDir)
            }

            named<Jar>(JAVADOC_JAR_TASK) {
                dependsOn(dokkaJavadoc)
                from(javadocDestDir)
            }
        }

        project.extensions.configure<PublishingExtension> {
            publications {
                register(publicationName, MavenPublication::class.java) {
                    artifactId = determinedPublication.artifactId
                    groupId = determinedPublication.groupId

                    from(project.components["java"])

                    pom {
                        name.set(determinedPublication.packageName)
                        description.set(determinedPublication.description)

                        project.applyPomConfigurators(this)
                    }
                }
            }
        }

        val thisProject = project

        project.rootProject.tasks {
            if (determinedPublication.publishToSonatype && thisProject.getClosestProperty { sonatypeSettings() } != null) {
                named(PUBLISH_TO_SONATYPE_WITH_EXCLUDING_TASK) {
                    dependsOn(thisProject.tasks[getPublishTaskName(SONATYPE_REPOSITORY_NAME, publicationName)])
                }
            }
        }

        project.getClosestProperty { signingCredentials() }?.apply {
            project.extensions.configure<SigningExtension> {
                sign(project.extensions.getByType<PublishingExtension>().publications[publicationName])

                @Suppress("UnstableApiUsage")
                useInMemoryPgpKeys(key, privateKey, keyPassphrase)
            }
        }
    }

    companion object {
        const val NAME = "kotlinPublications"

        private const val SONATYPE_REPOSITORY_NAME = "Sonatype"
        private const val PUBLISH_TO_SONATYPE_WITH_EXCLUDING_TASK = "publishToSonatypeWithExcluding"
        private const val PUBLISH_TO_SONATYPE_AND_RELEASE_TASK = "publishToSonatypeAndRelease"
        private const val PUBLISH_LOCAL_TASK = "publishLocal"
        private const val CLOSE_AND_RELEASE_TASK = "closeAndReleaseStagingRepository"

        private const val DOKKA_HTML_TASK = "dokkaHtml"
        private const val DOKKA_JAVADOC_TASK = "dokkaJavadoc"
        private const val JAVADOC_JAR_TASK = "javadocJar"
    }
}
