package org.jetbrains.kotlinx.publisher.testUtil

import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.should
import java.io.File

@Suppress("unused")
fun File.shouldContainAllFiles(vararg names: String) = this should containAllFiles(*names)
fun File.shouldContainAllFiles(names: List<String>) = this should containAllFiles(*names.toTypedArray())
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
            { "Directory $value should contain following files that have not been detected: ${listFiles(notContained)}" },
            {
                "Directory $value should not contain following files that have been detected: ${listFiles(contained)}"
            }
        )
    }
}
