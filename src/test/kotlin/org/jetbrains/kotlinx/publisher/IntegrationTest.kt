package org.jetbrains.kotlinx.publisher

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.jetbrains.kotlinx.publisher.testUtil.parsePom
import org.jetbrains.kotlinx.publisher.testUtil.shouldContainAllFiles
import org.jetbrains.kotlinx.publisher.testUtil.testPublishLocal
import org.jetbrains.kotlinx.publisher.testUtil.testTasks
import org.junit.jupiter.api.Test

class IntegrationTest {

    @Test
    fun `test publishLocal in simple project with Kotlin DSL`() = testPublishLocal("simpleKotlinDsl") {
        buildDir.shouldContainAllFiles(
            pathsFactory.allArchives("pub-dev")
        )
    }

    @Test
    fun `test publishLocal in simple project with Groovy DSL`() = testPublishLocal("simpleGroovyDsl") {
        buildDir.shouldContainAllFiles(
            pathsFactory.allArchives("pub-dev-groovy")
        )
    }

    @Test
    fun `check POM modification like in kernel`() = testPublishLocal("kernelLikePublication") {
        val artifactId = "kotlin-jupyter-kernel"
        val customPaths = pathsFactory.withGroup("org.jetbrains.kotlinx")

        buildDir.shouldContainAllFiles(
            customPaths.allArchives(artifactId)
        )

        with(buildDir.resolve(customPaths.pom(artifactId)).parsePom()) {
            licenses.single().apply {
                name shouldBe "The Apache Software License, Version 2.0"
            }
            developers.shouldHaveSize(2)
            version shouldBe options.scriptOptions.version
        }
    }

    @Test
    fun `check dataframe-like publication that uses KSP`() = testPublishLocal("dataframeLikePublication") {
        val dfArtifact = "dataframe"
        val procArtifact = "symbol-processor"
        val procPaths = pathsFactory.withGroup("org.jetbrains.kotlinx.dataframe").withVersion("0.0.2")

        buildDir.shouldContainAllFiles(
            pathsFactory.allArchives(dfArtifact) + procPaths.allArchives(procArtifact)
        )

        with(buildDir.resolve(pathsFactory.pom(dfArtifact)).parsePom()) {
            developers.shouldHaveSize(1)
            version shouldBe options.scriptOptions.version
            inceptionYear shouldBe "2021"
            description shouldBe "Data processing in Kotlin"
        }
    }

    @Test
    fun `check tasks task for docs plugin`() = testTasks("publishDocs") {
        buildResult.output shouldContain "publishDocs - Publishes Dokka HTML output to git repository https://my.git, branch master"
    }
}
