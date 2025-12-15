import me.champeau.gradle.igp.gitRepositories

rootProject.name = "Dicio"
include(":app")
include(":skill")
include(":numbers")  // Incluye el módulo numbers
includeBuild("sentences-compiler-plugin")
includeBuild("unicode-cldr-plugin")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("me.champeau.includegit") version "0.1.5"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

val includeGitRepos = listOf(
    org.stypox.dicio.IncludeGitRepo(
        name = "dicio-numbers",
        uri = "https://github.com/abdel-monzon/dicio-numbers",
        projectPath = ":numbers",
        commit = "master",  // Usa la rama master
    ),
    org.stypox.dicio.IncludeGitRepo(
        name = "dicio-sentences-compiler",
        uri = "https://github.com/Stypox/dicio-sentences-compiler",
        projectPath = ":sentences_compiler",
        commit = "main",  // Cambia esto según el commit necesario
    ),
)

val localProperties = java.util.Properties().apply {
    try {
        load(java.io.FileInputStream(java.io.File(rootDir, "local.properties")))
    } catch (e: Throwable) {
        println("Warning: can't read local.properties: $e")
    }
}

if (localProperties.getOrDefault("useLocalDicioLibraries", "") == "true") {
    for (repo in includeGitRepos) {
        includeBuild("../${repo.name}") {
            dependencySubstitution {
                substitute(module("git.included.build:${repo.name}")).using(project(repo.projectPath))
            }
        }
    }
} else {
    gitRepositories {
        for (repo in includeGitRepos) {
            include(repo.name) {
                uri.set(repo.uri)
                if (repo.name == "dicio-numbers") {
                    branch.set("master")
                } else {
                    commit.set(repo.commit)
                }
                autoInclude.set(false)
                includeBuild("") {
                    dependencySubstitution {
                        substitute(module("git.included.build:${repo.name}")).using(project(repo.projectPath))
                    }
                }
            }
        }
    }
}

