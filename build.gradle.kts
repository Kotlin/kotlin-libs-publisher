plugins {
    id("org.jlleitschuh.gradle.ktlint") version "10.0.0"
    id("com.gradle.plugin-publish") version "0.12.0"
    `java-gradle-plugin`
    `kotlin-dsl`
}

group = "ru.ileasile"
version = "0.0.2"

repositories {
    jcenter()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:1.4.20")
    implementation("io.codearte.gradle.nexus:gradle-nexus-staging-plugin:0.22.0")
    implementation("de.marcphilipp.gradle:nexus-publish-plugin:0.4.0")

    // For maven-publish
    implementation(gradleApi())
}

val publishingPlugin = "publishing"
val docPlugin = "doc"

gradlePlugin {
    plugins {
        create(publishingPlugin) {
            id = "ru.ileasile.kotlin.publisher"
            implementationClass = "ru.ileasile.kotlin.ApiPublishGradlePlugin"
        }
        create(docPlugin) {
            id = "ru.ileasile.kotlin.doc"
            implementationClass = "ru.ileasile.kotlin.DocGradlePlugin"
        }
    }
}

pluginBundle {
    // These settings are set for the whole plugin bundle
    website = "https://github.com/ileasile/kotlin-libs-publisher"
    vcsUrl = website

    (plugins) {
        publishingPlugin {
            displayName = "Kotlin libs publisher plugin"
            description = displayName
            tags = listOf("kotlin")
        }

        docPlugin {
            displayName = "Kotlin libs documenting plugin"
            description = displayName
            tags = listOf("kotlin")
        }
    }
}
