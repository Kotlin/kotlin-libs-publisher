plugins {
    id("org.jlleitschuh.gradle.ktlint") version "10.0.0"
    id("com.gradle.plugin-publish") version "0.12.0"
    `java-gradle-plugin`
    `kotlin-dsl`
}

group = "ru.ileasile"
version = detectVersion()

fun detectVersion(): String {
    val buildNumber = rootProject.findProperty("build.number") as String?
    return if (buildNumber != null) {
        if (hasProperty("build.number.detection")) {
            "$version-dev-$buildNumber"
        } else {
            buildNumber
        }
    } else if (hasProperty("release")) {
        version as String
    } else {
        "$version-dev"
    }
}

val detectVersionForTC by tasks.registering {
    doLast {
        println("##teamcity[buildNumber '$version']")
    }
}

val junitVersion: String by project

repositories {
    jcenter()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:1.4.30")
    implementation("io.github.gradle-nexus:publish-plugin:1.0.0")

    // For maven-publish
    implementation(gradleApi())

    // Test dependencies: kotlin-test and Junit 5
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("io.kotlintest:kotlintest-assertions:3.4.2")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
    outputs.upToDateWhen { false }
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
