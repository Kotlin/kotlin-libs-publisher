package org.jetbrains.kotlinx.publisher

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.process.ExecResult
import org.gradle.process.ExecSpec
import org.jetbrains.dokka.gradle.AbstractDokkaTask
import org.jetbrains.dokka.gradle.DokkaPlugin

@Suppress("unused")
class DocGradlePlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        pluginManager.run {
            apply(DokkaPlugin::class.java)
            // Preparing for Dokka V2 plugin
            // apply(DokkaJavadocPlugin::class.java)
        }

        val dokkaTaskName = if (subprojects.isEmpty()) DOKKA_HTML_TASK else DOKKA_HTML_MULTI_MODULE_TASK
        val dokkaTask = tasks.named<AbstractDokkaTask>(dokkaTaskName).get()
        val dokkaOutput = dokkaTask.outputDirectory.get()
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
                copy {
                    from(dokkaOutput)
                    into(copyDestDir)
                }

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
        private const val DOKKA_HTML_TASK = "dokkaHtml"
        private const val DOKKA_HTML_MULTI_MODULE_TASK = "dokkaHtmlMultiModule"
    }
}
