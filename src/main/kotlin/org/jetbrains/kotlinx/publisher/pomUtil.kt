package org.jetbrains.kotlinx.publisher

import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.publish.maven.MavenPomDeveloperSpec
import org.gradle.api.publish.maven.MavenPomLicenseSpec

@Suppress("unused")
fun MavenPomDeveloperSpec.developer(id: String, name: String, email: String) = developer {
    this.id.set(id)
    this.name.set(name)
    this.email.set(email)
}

@Suppress("unused")
fun MavenPomLicenseSpec.apache2() = license {
    name.set("The Apache Software License, Version 2.0")
    url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
    distribution.set("repo")
}

@Suppress("unused")
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
