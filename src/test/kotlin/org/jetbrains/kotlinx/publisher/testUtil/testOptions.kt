package org.jetbrains.kotlinx.publisher.testUtil

import org.gradle.testkit.runner.BuildResult
import java.io.File

typealias BuildResultChecker = BuildResultEx.() -> Unit

class TestOptions(
    val testDir: String,
    val cmdLine: List<String>,
    val checker: BuildResultChecker = {},
    val scriptOptions: BuildScriptOptions = BuildScriptOptions.DEFAULT,
    val withSimpleSources: Boolean = true,
)

class BuildResultEx(
    val buildResult: BuildResult,
    val buildDir: File,
    val pathsFactory: TestPathsFactory,
    val options: TestOptions,
)

data class BuildScriptOptions(
    val group: String = "kotlin.publisher.samples",
    val version: String = "0.0.1",
    val kotlinVersion: String = "1.5.31",
    val localPublishRepo: String = "artifacts/maven",
) {
    companion object {
        val DEFAULT = BuildScriptOptions()
    }
}
