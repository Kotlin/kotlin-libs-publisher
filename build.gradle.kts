plugins {
    `java-gradle-plugin`
    alias(libs.plugins.plugin.publish)
    alias(libs.plugins.bootstrapLibsPublisher)
    alias(libs.plugins.ktlint)
    `kotlin-dsl`
}

group = "org.jetbrains.kotlin"
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

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(libs.dokkaGradlePlugin.html)
    implementation(libs.nexusPublishPlugin)
    implementation(libs.kotlinGradlePlugin)

    // For maven-publish
    implementation(gradleApi())

    // Test dependencies: kotlin-test and Junit 5
    testImplementation(kotlin("test"))
    testImplementation(libs.test.junit.api)
    testImplementation(libs.kotest.assertions)
    testImplementation(gradleTestKit())

    testRuntimeOnly(libs.test.junit.engine)
}

java {
    withSourcesJar()
    withJavadocJar()

    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

tasks.withType<JavaCompile> {
    sourceCompatibility = JavaVersion.VERSION_1_8.toString()
    targetCompatibility = JavaVersion.VERSION_1_8.toString()
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
    outputs.upToDateWhen { false }
}

tasks.runKtlintCheckOverMainSourceSet {
    dependsOn(tasks.runKtlintCheckOverKotlinScripts)
}

tasks.runKtlintFormatOverMainSourceSet {
    dependsOn(tasks.runKtlintFormatOverKotlinScripts)
}

tasks.runKtlintFormatOverTestSourceSet {
    dependsOn(tasks.runKtlintFormatOverKotlinScripts)
}

val publishingPlugin = "publishing"
val docPlugin = "doc"

gradlePlugin {
    website.set("https://github.com/Kotlin/kotlin-libs-publisher")
    vcsUrl.set(website.get())
    plugins {
        create(publishingPlugin) {
            displayName = "Kotlin Libraries Publisher"
            description = "Plugin that allows configuring maven publications with less code, especially for multi-module libraries."
            id = "org.jetbrains.kotlin.libs.publisher"
            implementationClass = "org.jetbrains.kotlinx.publisher.ApiPublishGradlePlugin"
            tags.set(listOf("kotlin", "publishing"))
        }
        create(docPlugin) {
            displayName = "Kotlin Libraries Documentation Publisher"
            description = "Plugin that enables publishing the HTML version of Dokka-generated documentation to a Git repository, allowing it to be rendered via GitHub Pages."
            id = "org.jetbrains.kotlin.libs.doc"
            implementationClass = "org.jetbrains.kotlinx.publisher.DocGradlePlugin"
            tags.set(listOf("kotlin", "documentation"))
        }
    }
}

kotlinPublications {
    localRepositories {
        defaultLocalMavenRepository()
    }
    signing {
        // We do not need signing for the Gradle Plugin Portal
        isRequired = false
    }
}
