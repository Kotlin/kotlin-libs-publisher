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
    implementation(libs.dokkaGradlePlugin)
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
            id = "org.jetbrains.kotlin.libs.publisher"
            implementationClass = "org.jetbrains.kotlinx.publisher.ApiPublishGradlePlugin"
            tags.set(listOf("kotlin", "publishing"))
        }
        create(docPlugin) {
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
}

tasks.whenTaskAdded {
    val task = this
    if (task.name == "generateMetadataFileForPluginMavenPublication") {
        task.mustRunAfter(":publishPluginJar", ":publishPluginJavaDocsJar")
    }
}
