[![JetBrains incubator project](https://jb.gg/badges/incubator.svg)](https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/org.jetbrains.kotlin.libs.publisher?label=publisher%20plugin)](https://plugins.gradle.org/plugin/org.jetbrains.kotlin.libs.publisher)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/org.jetbrains.kotlin.libs.doc?label=docs%20plugin)](https://plugins.gradle.org/plugin/org.jetbrains.kotlin.libs.doc)
[![GitHub](https://img.shields.io/github/license/Kotlin/kotlin-libs-publisher)](LICENSE.txt)

# Kotlin libraries publishing plugin: simple publishing setup for Kotlin libraries

Publish Kotlin libraries to local Maven repositories or to Sonatype (Maven Central) with ease!

## Publishing plugin

This plugin allows configuring maven publications with less code, especially for multi-module libraries.
Note that it's better to specify plugin version `settings.gradle[.kts]` or via version catalogs feature.

### Setup for the root project

Kotlin DSL:

```kotlin
import org.jetbrains.kotlinx.publisher.apache2
import org.jetbrains.kotlinx.publisher.developer
import org.jetbrains.kotlinx.publisher.githubRepo

plugins {
    kotlin("libs.publisher")
}

kotlinPublications {
    // Default group for all publications
    defaultGroup.set("org.jetbrains.kotlinx")

    // Prefix for artifacts for all publications
    defaultArtifactIdPrefix.set("kotlin-jupyter-")

    // Set to false if you want to publish empty Javadoc JARs. Maven Central is OK with it
    fairDokkaJars.set(false)

    // Signing credentials. You will not be able to publish an artifact to Maven Central without signing
    signingCredentials(
        "key.id",
        "key.private",
        "key.passphrase"
    )
    
    sonatypeSettings(
        "some user",
        "some password",
        // Description that will be shown on your Sonatype admin page
        "repository description" 
    )

    pom {
        // Use this convenience extension to set up all needed URLs
        // for the POM in case you're using GitHub
        githubRepo("github_user", "github_repo")

        // The year your library was first time published to a public repository
        inceptionYear.set("2021") 

        licenses {
            // You can contribute extension methods for licences other than Apache 2.0
            apache2()
        }

        developers {
            developer("nickname", "name", "email")
        }
    }

    localRepositories {
        // Default location for the local repository is build/artifacts/maven/
        defaultLocalMavenRepository() 
    }

    publication {
        // By default, publication name is an artifact name. In this case, artifact name 
        // will be kotlin-jupyter-kernel (prefix + name)
        publicationName.set("kernel")

        // Description that will appear in POM and on Maven Central search site
        description.set("Kotlin Jupyter kernel published as artifact") 
    }
}
```

Groovy DSL:

```groovy
plugins {
    id("org.jetbrains.kotlin.libs.publisher")
}

kotlinPublications {
    // Default group for all publications
    defaultGroup.set("org.jetbrains.kotlinx")

    // Prefix for artifacts for all publications
    defaultArtifactIdPrefix.set("kotlin-jupyter-")

    // Set to false if you want to publish empty Javadoc JARs. Maven Central is OK with it
    fairDokkaJars.set(false)

    // Signing credentials. You will not be able to publish an artifact to Maven Central without signing
    signingCredentials(
        "key.id",
        "key.private",
        "key.passphrase"
    )
    
    sonatypeSettings(
        "some user",
        "some password",
        // Description that will be shown on your Sonatype admin page
        "repository description" 
    )

    pom {
        use(org.jetbrains.kotlinx.publisher.PomUtilKt) {
            // Use this convenience extension to setup all needed URLs
            // for the POM in case you're using GitHub
            githubRepo("github_user", "github_repo")

            // The year your library was first time published to a public repository
            inceptionYear.set("2021")

            licenses {
                // You can contribute extension methods for licences other than Apache 2.0
                apache2()
            }

            developers {
                developer("nickname", "name", "email")
            }
        }
    }

    localRepositories {
        // A place where the artifacts are published with publishLocal task
        maven {
            url = project.file(new File(buildDir, "artifacts/maven")).toURI()
        }
    }

    publication {
        // By default, publication name is an artifact name. In this case, artifact name 
        // will be kotlin-jupyter-kernel (prefix + name)
        publicationName.set("kernel")

        // Description that will appear in POM and on Maven Central search site
        description.set("Kotlin Jupyter kernel published as artifact") 
    }
}
```

### Setup for subprojects

Setup is the same for Kotlin and Groovy DSL:

```kotlin
plugins {
    id("org.jetbrains.kotlin.libs.publisher")
}

kotlinPublications {
    publication {
        // Artifact name for this publication will be kotlin-jupyter-api (prefix + name)
        publicationName.set("api")

        // Description that will appear in POM and on Maven Central search site
        description.set("API artifact")
    }
}
```


## Docs plugin

This plugin allows publishing HTML version of documentation generated with dokka
to git repository that may be rendered with GitHub Pages.

### Setup for the root project

Setup is the same for Kotlin and Groovy DSL:

```kotlin
plugins {
    id("org.jetbrains.kotlin.libs.doc")
}

tasks.publishDocs {
    // Git repository to commit. 
    docsRepoUrl.set("https://username:password@github.com/username/repository.git")
    
    // Branch to commit
    branchName.set("master")
    
    // Username of the committer
    username.set("robot")
    
    // Email of the committer
    email.set("robot@jetbrains.com") 
}
```
