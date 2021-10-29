package ru.ileasile.kotlin

import io.kotlintest.Matcher
import io.kotlintest.MatcherResult
import io.kotlintest.should
import org.gradle.testkit.runner.BuildResult
import java.io.File

class BuildResultEx(
    val buildResult: BuildResult,
    val buildDir: File,
)

typealias BuildResultChecker = BuildResultEx.() -> Unit

fun File.shouldContainAllFiles(vararg names: String) = this should containAllFiles(*names)
fun containAllFiles(vararg names: String) = object : Matcher<File> {
    override fun test(value: File): MatcherResult {
        val contents = value.walkTopDown().toSet()
        val files = names.map { value.resolve(it) }
        val contained = mutableSetOf<File>()
        val notContained = mutableSetOf<File>()

        val passed = value.isDirectory && files.fold(true) { acc, file ->
            (file in contents).also { contains ->
                if (contains) contained.add(file)
                else notContained.add(file)
            } && acc
        }
        fun listFiles(files: Iterable<File>): String {
            return files.joinToString("\n", "\n")
        }

        return MatcherResult(
            passed,
            "Directory $value should contain following files that have not been detected: ${listFiles(notContained)}",
            "Directory $value should not contain following files that have been detected: ${listFiles(contained)}"
        )
    }
}

fun <T> File.takeIfGradleScript(action: () -> T): T? {
    return if (name.contains(".gradle")) action() else null
}
