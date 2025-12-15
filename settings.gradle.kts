rootProject.name = "Dicio"
include(":app")
include(":skill")
include(":numbers")  // Incluye el módulo numbers

// Configura el directorio del módulo numbers
project(":numbers").projectDir = file("../dicio-numbers")

includeBuild("sentences-compiler-plugin")
includeBuild("unicode-cldr-plugin")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

