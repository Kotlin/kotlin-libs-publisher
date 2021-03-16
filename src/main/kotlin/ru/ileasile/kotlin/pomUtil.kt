package ru.ileasile.kotlin

import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.publish.maven.MavenPomDeveloperSpec
import org.gradle.api.publish.maven.MavenPomLicenseSpec

fun MavenPomDeveloperSpec.developer(id: String, name: String, email: String) = developer {
    this.id.set(id)
    this.name.set(name)
    this.email.set(email)
}

fun MavenPomLicenseSpec.apache2() = license {
    name.set("The Apache Software License, Version 2.0")
    url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
    distribution.set("repo")
}

fun MavenPom.githubRepo(organization: String, name: String) {
    val webUrl = "https://github.com/$organization/$name"
    val scmUrl = "scm:git:$webUrl.git"

    url.set(webUrl)
    scm {
        url.set(webUrl)
        connection.set(scmUrl)
        developerConnection.set(scmUrl)
    }
}
