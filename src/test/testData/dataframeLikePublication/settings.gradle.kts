rootProject.name = "dataframe"

include("plugins:symbol-processor")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("ksp", "1.5.30-1.0.0")
            plugin("ksp-gradle", "com.google.devtools.ksp").versionRef("ksp")
            library("ksp-api", "com.google.devtools.ksp", "symbol-processing-api").versionRef("ksp")
        }
    }
}

pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}
