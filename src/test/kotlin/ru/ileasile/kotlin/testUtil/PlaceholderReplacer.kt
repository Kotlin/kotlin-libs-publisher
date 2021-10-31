package ru.ileasile.kotlin.testUtil

import java.io.File

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
