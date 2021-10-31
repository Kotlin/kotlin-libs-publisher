package ru.ileasile.kotlin.testUtil

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
