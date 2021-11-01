package org.jetbrains.kotlinx.publisher.testUtil

import org.gradle.internal.impldep.org.apache.maven.model.Model
import org.gradle.internal.impldep.org.apache.maven.model.io.xpp3.MavenXpp3Reader
import org.gradle.testkit.runner.GradleRunner
import java.io.File
import java.nio.file.Files

private const val PUBLISH_LOCAL_TASK = "publishLocal"
private const val TASKS_TASK = "tasks"

private val TEST_DATA_DIR = File("src/test/testData")
private val DEFAULT_TEST_BUILD_OPTIONS = listOf(
    "--stacktrace",
    "--info"
)

val File.isGradleFile: Boolean get() {
    return isFile && (name.contains(".gradle") || name == "gradle.properties")
}

fun <T> File.takeIfGradleScript(action: () -> T): T? {
    return if (isGradleFile) action() else null
}

fun File.parsePom(): Model {
    return MavenXpp3Reader().read(reader())
}

fun <T> underTempDir(options: TestOptions, dir: File, action: (File) -> T): T {
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

fun createSimpleSources(dest: File) {
    dest.walkTopDown().filter { it.isGradleFile }.forEachIndexed { counter, scriptFile ->
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

fun doTest(options: TestOptions) {
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

fun testPublishLocal(testName: String, checker: BuildResultChecker) = doTest(testName, PUBLISH_LOCAL_TASK, checker)
fun testTasks(testName: String, checker: BuildResultChecker) = doTest(testName, TASKS_TASK, checker)

fun doTest(testName: String, taskName: String, checker: BuildResultChecker) = doTest(
    TestOptions(
        testName,
        listOf(taskName) + DEFAULT_TEST_BUILD_OPTIONS,
        checker,
    )
)
