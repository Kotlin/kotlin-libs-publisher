package ru.ileasile.kotlin

import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.shouldBe
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Files

class IntegrationTest {

    @Test
    fun `test publishLocal in simple project with Kotlin DSL`() = doTaskTest("simpleKotlinDsl", PUBLISH_LOCAL_TASK) {
        buildDir.shouldContainAllFiles(
            pathsFactory.allArchives("pub-dev")
        )
    }

    @Test
    fun `test publishLocal in simple project with Groovy DSL`() = doTaskTest("simpleGroovyDsl", PUBLISH_LOCAL_TASK) {
        buildDir.shouldContainAllFiles(
            pathsFactory.allArchives("pub-dev-groovy")
        )
    }

    @Test
    fun `check POM modification like in kernel`() = doTaskTest("kernelLikePublication", PUBLISH_LOCAL_TASK) {
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
    fun `check dataframe-like publication that uses KSP`() = doTaskTest("dataframeLikePublication", PUBLISH_LOCAL_TASK) {
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

    private fun doTaskTest(testName: String, taskName: String, checker: BuildResultChecker) = doTest(
        TestOptions(
            testName,
            listOf(taskName) + DEFAULT_TEST_BUILD_OPTIONS,
            checker,
        )
    )

    private fun doTest(options: TestOptions) {
        underTempDir(options, TEST_DATA_DIR.resolve(options.testDir)) { projectDir ->
            val buildResult = GradleRunner.create()
                .withProjectDir(projectDir)
                .withPluginClasspath()
                .withArguments(options.cmdLine)
                .forwardOutput()
                .build()

            val pathsFactory = TestPathsFactory(options.scriptOptions)

            options.checker(
                BuildResultEx(
                    buildResult,
                    projectDir.resolve("build"),
                    pathsFactory,
                    options,
                )
            )
        }
    }

    private fun <T> underTempDir(options: TestOptions, dir: File, action: (File) -> T): T {
        val tempDir = Files.createTempDirectory("kotlin-libs-publisher-test-project").toFile()
        tempDir.deleteRecursively()

        for (src in dir.walkTopDown()) {
            val relPath = src.toRelativeString(dir)
            val dest = File(tempDir, relPath)
            if (src.isDirectory) dest.mkdirs()
            else {
                PlaceholderReplacer(options.scriptOptions, src).copyWithReplacingTo(dest)
            }
        }
        if (options.withSimpleSources) {
            createSimpleSources(tempDir)
        }

        val result = action(tempDir)

        tempDir.deleteRecursively()
        return result
    }

    private fun createSimpleSources(dest: File) {
        dest.walkTopDown().filter { it.isGradleScript }.forEachIndexed { counter, scriptFile ->
            val ktClassFile = scriptFile.parentFile.resolve("src/kotlin/main/my$counter/MyClass$counter.kt")
            ktClassFile.parentFile.mkdirs()
            ktClassFile.writeText(
                """
                package my$counter
                class MyClass$counter(val x: Int = 5)
                """.trimIndent()
            )
        }
    }

    companion object {
        private val TEST_DATA_DIR = File("src/test/testData")

        private const val PUBLISH_LOCAL_TASK = "publishLocal"
        private val DEFAULT_TEST_BUILD_OPTIONS = listOf(
            "--stacktrace",
            "--info"
        )
    }
}
