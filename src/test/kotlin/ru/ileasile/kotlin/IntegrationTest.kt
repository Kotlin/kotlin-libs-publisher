package ru.ileasile.kotlin

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Files

class IntegrationTest {

    @Test
    fun `test publishLocal in simple project with Kotlin DSL`() = doTaskTest("simpleKotlinDsl", PUBLISH_LOCAL_TASK) {
        val artifactId = "pub-dev"
        buildDir.shouldContainAllFiles(
            jarArtifact(artifactId),
            sourcesArtifact(artifactId),
            javadocArtifact(artifactId),
        )
    }

    @Test
    fun `test publishLocal in simple project with Groovy DSL`() = doTaskTest("simpleGroovyDsl", PUBLISH_LOCAL_TASK) {
        val artifactId = "pub-dev-groovy"
        buildDir.shouldContainAllFiles(
            jarArtifact(artifactId),
            sourcesArtifact(artifactId),
            javadocArtifact(artifactId),
        )
    }

    private fun doTaskTest(testName: String, taskName: String, checker: BuildResultChecker) = doTest(
        TestOptions(
            testName,
            listOf(taskName) + DEFAULT_TEST_BUILD_OPTIONS,
            checker,
        )
    )

    private class TestOptions(
        val testDir: String,
        val cmdLine: List<String>,
        val checker: BuildResultChecker = {},
    )

    private fun doTest(options: TestOptions) {
        underTempDir(TEST_DATA_DIR.resolve(options.testDir)) { projectDir ->
            val buildResult = GradleRunner.create()
                .withProjectDir(projectDir)
                .withPluginClasspath()
                .withArguments(options.cmdLine)
                .forwardOutput()
                .build()

            options.checker(
                BuildResultEx(
                    buildResult,
                    projectDir.resolve("build"),
                )
            )
        }
    }

    private fun <T> underTempDir(dir: File, action: (File) -> T): T {
        val tempDir = Files.createTempDirectory("kotlin-libs-publisher-test-project").toFile()
        tempDir.deleteRecursively()

        for (src in dir.walkTopDown()) {
            val relPath = src.toRelativeString(dir)
            val dest = File(tempDir, relPath)
            copyFile(src, dest)
        }
        val result = action(tempDir)

        tempDir.deleteRecursively()
        return result
    }

    private fun copyFile(src: File, dest: File) {
        if (src.isDirectory) dest.mkdirs()
        else {
            val text = src.readText()
            val replacedText = placeholders.entries.fold(text) { acc, (placeholder, replacer) ->
                val replacement = replacer(src)
                if (replacement == null) text
                else acc.replace(placeholder, replacement)
            }
            dest.writeText(replacedText)
        }
    }

    companion object {
        private val TEST_DATA_DIR = File("src/test/testData")

        private const val PUBLISH_LOCAL_TASK = "publishLocal"
        private val DEFAULT_TEST_BUILD_OPTIONS = listOf(
            "--stacktrace",
            "--info"
        )

        private const val DEFAULT_LOCAL_MAVEN_REPO_FOLDER = "artifacts/maven"
        private const val DEFAULT_TEST_GROUP = "ru.ileasile.samples"
        private const val DEFAULT_TEST_VERSION = "0.0.1"
        private const val DEFAULT_KOTLIN_VERSION = "1.5.31"

        private val placeholders: Map<String, (File) -> String?> = mapOf(
            "%GROUP_VERSION%" to {
                it.takeIfGradleScript {
                    """
                        group = "$DEFAULT_TEST_GROUP"
                        version = "$DEFAULT_TEST_VERSION"
                    """.trimIndent()
                }
            },
            "%KOTLIN_VERSION%" to {
                it.takeIfGradleScript {
                    DEFAULT_KOTLIN_VERSION
                }
            },
        )

        private fun artifact(artifactId: String, classifier: String, extension: String): String {
            return buildString {
                append("$DEFAULT_LOCAL_MAVEN_REPO_FOLDER/${DEFAULT_TEST_GROUP.replace(".", "/")}/")
                append("$artifactId/$DEFAULT_TEST_VERSION/$artifactId-$DEFAULT_TEST_VERSION")
                if (classifier.isNotEmpty()) append("-$classifier")
                append(".$extension")
            }
        }

        private fun jarArtifact(artifactId: String) = artifact(artifactId, "", "jar")
        private fun sourcesArtifact(artifactId: String) = artifact(artifactId, "sources", "jar")
        private fun javadocArtifact(artifactId: String) = artifact(artifactId, "javadoc", "jar")
    }
}
