import me.champeau.gradle.igp.gitRepositories
import org.eclipse.jgit.api.Git
import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.Properties

rootProject.name = "Dicio"
include(":app")
include(":skill")
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
    fun findInVersionCatalog(versionIdentifier: String): String {
        val regex = "^.*$versionIdentifier *= *\"([^\"]+)\".*$".toRegex()
        return File("gradle/libs.versions.toml")
            .readLines()
            .firstNotNullOf { regex.find(it)?.groupValues?.get(1) }
    }

    id("me.champeau.includegit") version findInVersionCatalog("includegitPlugin")
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

data class IncludeGitRepo(
    val name: String,
    val uri: String,
    val projectPath: String,
    val commit: String,
)

fun findInVersionCatalog(versionIdentifier: String): String {
    val regex = "^.*$versionIdentifier *= *\"([^\"]+)\".*$".toRegex()
    return File("gradle/libs.versions.toml")
        .readLines()
        .firstNotNullOf { regex.find(it)?.groupValues?.get(1) }
}

val includeGitRepos = listOf(
    IncludeGitRepo(
        name = "dicio-numbers",
        uri = "https://github.com/abdel-monzon/dicio-numbers",
        projectPath = ":numbers",
        commit = "master",
    ),
    IncludeGitRepo(
        name = "dicio-sentences-compiler",
        uri = "https://github.com/Stypox/dicio-sentences-compiler",
        projectPath = ":sentences_compiler",
        commit = "master",
    ),
)

val localProperties = Properties().apply {
    try {
        load(FileInputStream(File(rootDir, "local.properties")))
    } catch (e: Throwable) {
        println("Warning: can't read local.properties: $e")
    }
}

if (localProperties.getOrDefault("useLocalDicioLibraries", "") == "true") {
    for (repo in includeGitRepos) {
        includeBuild("../${repo.name}") {
            dependencySubstitution {
                substitute(module("git.included.build:${repo.name}"))
                    .using(project(repo.projectPath))
            }
        }
    }

} else {
    for (repo in includeGitRepos) {
        val file = File("$rootDir/checkouts/${repo.name}")
        if (file.isDirectory) {
            val git = Git.open(file)
            val sameRemote = git.remoteList().call()
                .any { rem -> rem.urIs.any { uri -> uri.toString() == repo.uri } }
            if (sameRemote) {
                git.fetch().call()
            } else {
                println("Git: remote for ${repo.name} changed, deleting the current folder")
                file.deleteRecursively()
            }
        }
    }

    gitRepositories {
        for (repo in includeGitRepos) {
            include(repo.name) {
                uri.set(repo.uri)
                branch.set("master")
                autoInclude.set(false)
                includeBuild("") {
                    dependencySubstitution {
                        substitute(module("git.included.build:${repo.name}"))
                            .using(project(repo.projectPath))
                    }
                }
            }
        }
    }
}
