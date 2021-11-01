plugins {
    kotlin("libs.doc")
    kotlin("jvm") version "%KOTLIN_VERSION%"
}

%GROUP_VERSION%

repositories {
    mavenCentral()
}

tasks.publishDocs {
    docsRepoUrl.set("https://my.git")
    branchName.set("master")
    username.set("robot")
    email.set("robot@jetbrains.com")
}
