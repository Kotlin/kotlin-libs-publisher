package org.jetbrains.kotlinx.publisher

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.process.ExecResult
import org.gradle.process.ExecSpec
import org.intellij.lang.annotations.Language
import org.jetbrains.dokka.gradle.DokkaBasePlugin
import org.jetbrains.dokka.gradle.formats.DokkaHtmlPlugin
import org.jetbrains.dokka.gradle.tasks.DokkaGenerateTask

@Suppress("unused")
class DocGradlePlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        pluginManager.run {
            apply(DokkaBasePlugin::class.java)
            apply(DokkaHtmlPlugin::class.java)
        }

        val dokkaTaskName = DOKKA_HTML_TASK
        val dokkaTask = tasks.named<DokkaGenerateTask>(dokkaTaskName).get()
        val docRepoDir = layout.buildDirectory.file("docRepo").get().asFile
        docRepoDir.deleteRecursively()

        fun execGit(vararg args: String, configure: ExecSpec.() -> Unit = {}): ExecResult {
            return providers.exec {
                this.executable = "git"
                this.args = args.asList()
                this.workingDir = docRepoDir

                configure()
            }.result.get()
        }

        tasks.register<PublishDocsTask>("publishDocs") {
            group = PUBLISHING_GROUP
            dependsOn(dokkaTask)

            doLast {
                val repoUrl = docsRepoUrl.get()
                val branchName = this@register.branchName.get()

                docRepoDir.mkdirs()
                execGit("init")
                execGit("config", "user.email", email.get())
                execGit("config", "user.name", username.get())
                execGit("pull", repoUrl, branchName)

                val copyDestDir = docRepoDir.resolve("docs")
                copyDestDir.deleteRecursively()

                val subfolders = mutableListOf<String>()

                for (project in rootProject.allprojects) {
                    val fromDir = project.dokkaHtmlOutputDirectory.get()
                        .takeIf { it.asFile.exists() }
                        ?: continue

                    val subfolder = project.name
                    subfolders += subfolder
                    copy {
                        from(fromDir)
                        into(copyDestDir.resolve(subfolder))
                        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
                    }
                }
                val indexHtml = createIndexHtml(subfolders)
                copyDestDir.resolve("index.html").writeText(indexHtml)

                execGit("add", ".")
                val commitResult = execGit("commit", "-m", "[AUTO] Update docs: $version") {
                    isIgnoreExitValue = true
                }
                if (commitResult.exitValue == 0) {
                    execGit("push", "-u", repoUrl, branchName)
                }
            }
        }
    }

    companion object {
        private const val DOKKA_HTML_TASK = "dokkaGeneratePublicationHtml"
    }
}

private fun createIndexHtml(subfolders: MutableList<String>): String {
    @Language("HTML")
    val html = """
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <title>Documentation Index</title>
            <style>
                body { font-family: Arial, sans-serif; margin: 40px; }
                h1 { color: #333; }
                ul { list-style-type: none; padding: 0; }
                li { margin: 10px 0; }
                a { color: #2962FF; text-decoration: none; }
                a:hover { text-decoration: underline; }
            </style>
        </head>
        <body>
            <h1>Documentation Index</h1>
            <ul>
                ${
    subfolders.sorted().joinToString("\n") { subfolder ->
        "            <li><a href=\"$subfolder/\">$subfolder</a></li>"
    }
    }
            </ul>
        </body>
        </html>
    """.trimIndent()

    return html
}
