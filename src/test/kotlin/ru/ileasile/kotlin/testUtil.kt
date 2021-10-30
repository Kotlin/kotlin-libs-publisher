package ru.ileasile.kotlin

import io.kotlintest.Matcher
import io.kotlintest.MatcherResult
import io.kotlintest.should
import org.gradle.internal.impldep.org.apache.maven.model.Model
import org.gradle.internal.impldep.org.apache.maven.model.io.xpp3.MavenXpp3Reader
import org.gradle.testkit.runner.BuildResult
import java.io.File

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

typealias BuildResultChecker = BuildResultEx.() -> Unit

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
            "Directory $value should contain following files that have not been detected: ${listFiles(notContained)}",
            "Directory $value should not contain following files that have been detected: ${listFiles(contained)}"
        )
    }
}

val File.isGradleScript: Boolean get() {
    return isFile && name.contains(".gradle")
}

fun <T> File.takeIfGradleScript(action: () -> T): T? {
    return if (isGradleScript) action() else null
}

fun File.parsePom(): Model {
    return MavenXpp3Reader().read(reader())
}

data class BuildScriptOptions(
    val group: String = "ru.ileasile.samples",
    val version: String = "0.0.1",
    val kotlinVersion: String = "1.5.31",
    val localPublishRepo: String = "artifacts/maven",
) {
    companion object {
        val DEFAULT = BuildScriptOptions()
    }
}

class PlaceholderReplacer(
    private val options: BuildScriptOptions,
    private val src: File,
) {
    private val placeholders: List<Pair<String, () -> String?>> = listOf(
        "%GROUP_VERSION%" to {
            src.takeIfGradleScript {
                """
                    group = "${options.group}"
                    version = "${options.version}"
                """.trimIndent()
            }
        },
        "%KOTLIN_VERSION%" to {
            src.takeIfGradleScript {
                options.kotlinVersion
            }
        },
        "%LOCAL_PUBLISH_REPO%" to {
            src.takeIfGradleScript {
                options.localPublishRepo
            }
        },
    )

    fun copyWithReplacingTo(dest: File) {
        val text = src.readText()
        val replacedText = placeholders.fold(text) { acc, (placeholder, replacer) ->
            val replacement = replacer()
            if (replacement == null) text
            else acc.replace(placeholder, replacement)
        }
        dest.writeText(replacedText)
    }
}

class TestPathsFactory(
    private val options: BuildScriptOptions,
) {
    fun withGroup(group: String) = TestPathsFactory(options.copy(group = group))
    fun withVersion(version: String) = TestPathsFactory(options.copy(version = version))

    fun artifact(artifactId: String, classifier: String, extension: String): String {
        return buildString {
            with(options) {
                append("$localPublishRepo/${group.replace(".", "/")}/")
                append("$artifactId/$version/$artifactId-$version")
                if (classifier.isNotEmpty()) append("-$classifier")
                append(".$extension")
            }
        }
    }

    fun jar(artifactId: String) = artifact(artifactId, "", "jar")
    fun sources(artifactId: String) = artifact(artifactId, "sources", "jar")
    fun javadoc(artifactId: String) = artifact(artifactId, "javadoc", "jar")
    fun allArchives(artifactId: String) = listOf(
        jar(artifactId),
        sources(artifactId),
        javadoc(artifactId),
    )

    fun pom(artifactId: String) = artifact(artifactId, "", "pom")
}
