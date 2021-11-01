import org.jetbrains.kotlinx.publisher.apache2
import org.jetbrains.kotlinx.publisher.developer
import org.jetbrains.kotlinx.publisher.githubRepo

plugins {
    kotlin("libs.publisher")
    kotlin("jvm") version "%KOTLIN_VERSION%"
}

%GROUP_VERSION%

repositories {
    mavenCentral()
}

kotlinPublications {
    defaultGroup.set("org.jetbrains.kotlinx")
    defaultArtifactIdPrefix.set("kotlin-jupyter-")
    fairDokkaJars.set(false)

    sonatypeSettings(
        "some user",
        "some password",
        "kotlin-jupyter project, v. ${project.version}"
    )

    pom {
        githubRepo("Kotlin", "kotlin-jupyter")

        inceptionYear.set("2021")

        licenses {
            apache2()
        }

        developers {
            developer("nikitinas", "Anatoly Nikitin", "Anatoly.Nikitin@jetbrains.com")
            developer("ileasile", "Ilya Muradyan", "Ilya.Muradyan@jetbrains.com")
        }
    }

    localRepositories {
        localMavenRepository(buildDir.resolve("%LOCAL_PUBLISH_REPO%"))
    }

    publication {
        publicationName.set("kernel")
        description.set("Kotlin Jupyter kernel published as artifact")
    }
}
