plugins {
    kotlin("libs.publisher")
    kotlin("jvm") version "%KOTLIN_VERSION%"
}

%GROUP_VERSION%

repositories {
    mavenCentral()
}

kotlinPublications {
    publication {
        publicationName.set("pub")
        artifactId.set("pub-dev")
        description.set("Sample library publication")
    }

    localRepositories {
        defaultLocalMavenRepository()
    }
}
