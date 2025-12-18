import org.eclipse.jgit.api.Git
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.stypox.dicio.unicodeCldrPlugin.UnicodeCldrLanguagesTask

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath(libs.dicio.sentences.compiler.plugin)
        classpath(libs.dicio.unicode.cldr.plugin)
    }
}

plugins {
    alias(libs.plugins.com.android.application)
    alias(libs.plugins.org.jetbrains.kotlin.android)
    alias(libs.plugins.org.jetbrains.kotlin.plugin.compose)
    alias(libs.plugins.org.jetbrains.kotlin.plugin.parcelize)
    alias(libs.plugins.org.jetbrains.kotlin.plugin.serialization)
    alias(libs.plugins.com.google.devtools.ksp)
    alias(libs.plugins.com.google.dagger.hilt.android)
    alias(libs.plugins.com.google.protobuf)
    alias(libs.plugins.dicio.sentences.compiler.plugin)
    alias(libs.plugins.dicio.unicode.cldr.plugin)
}

// ⬇️ Aplicar configuración de firma CONSTANTE
apply(from = "signing.gradle")

android {
    namespace = "org.stypox.dicio"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.monzonabdel.miasistente"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 18
        versionName = "4.2"
        testInstrumentationRunner = "org.stypox.dicio.CustomTestRunner"

        vectorDrawables.useSupportLibrary = true

        ndk {
            abiFilters += arrayOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        }
    }

    buildTypes {
        debug {
            val normalizedGitBranch = gitBranch().replaceFirst("^[^A-Za-z]+", "").replace(Regex("[^0-9A-Za-z]+"), "")
            applicationIdSuffix = ".$normalizedGitBranch"
            versionNameSuffix = "-$normalizedGitBranch"

            val isScreenshotTest = (project.findProperty("android.testInstrumentationRunnerArguments.class") as? String)
                ?.contains("creenshot") == true
            if (!isScreenshotTest) {
                resValue("string", "app_name", "Dicio-${gitBranch()}")
            }
        }
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.toVersion(libs.versions.java.get())
        targetCompatibility = JavaVersion.toVersion(libs.versions.java.get())
    }

    kotlin {
        compilerOptions {
            jvmTarget = JvmTarget.fromTarget(libs.versions.java.get())
            freeCompilerArgs = listOf("-XXLanguage:+PropertyParamAnnotationDefaultTargetMode")
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
        compose = true
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

protobuf {
    protoc {
        artifact = libs.protobuf.protoc.get().toString()
    }
    plugins {
        generateProtoTasks {
            all().forEach {
                it.builtins {
                    create("kotlin") { option("lite") }
                    create("java") { option("lite") }
                }
            }
        }
    }
}

// workaround for https://github.com/google/ksp/issues/1590
val kspKotlinRegex = "^ksp(.*)Kotlin$".toRegex()
androidComponents {
    onVariants(selector().all()) { variant ->
        afterEvaluate {
            tasks.named(kspKotlinRegex::matches).configureEach {
                val capName = kspKotlinRegex.find(name)!!.groupValues[1]
                dependsOn(tasks.named("generate${capName}Proto"))
            }
        }
    }
}

tasks.withType(UnicodeCldrLanguagesTask::class) {
    unicodeCldrGitCommit = libs.versions.unicodeCldrGitCommit
}

dependencies {
    // -----------------------------------------------------------
    // ✅ RECORDATORIOS - DEPENDENCIAS NECESARIAS
    // -----------------------------------------------------------
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.androidx.work.runtime.ktx)

    // 👇 ESTA ES LA QUE FALTABA - Hilt + WorkManager
    implementation("androidx.hilt:hilt-work:1.0.0")
    ksp("androidx.hilt:hilt-compiler:1.0.0")

    implementation(libs.threetenabp)

    // Desugaring
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    // Dicio own libraries
    implementation(libs.dicio.numbers)
    implementation(project(":skill"))

    // Android
    implementation(libs.appcompat)

    // Compose
    implementation(libs.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.test.android.compose.ui.test.junit4)
    debugImplementation(libs.debug.compose.ui.tooling)
    debugImplementation(libs.debug.compose.ui.test.manifest)

    // Hilt Dependency Injection
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    ksp(libs.hilt.android.compiler)
    androidTestImplementation(libs.hilt.android.testing)
    testImplementation(libs.hilt.android.testing)
    testAnnotationProcessor(libs.hilt.android.compiler)

    // Protobuf and Datastore
    implementation(libs.protobuf.kotlin.lite)
    implementation(libs.protobuf.java.lite)
    implementation(libs.datastore)

    // Navigation
    implementation(libs.kotlin.serialization)
    implementation(libs.navigation)

    // Vosk
    implementation(libs.jna) { artifact { type = "aar" } }
    implementation(libs.vosk.android)

    // LiteRT / Tensorflow Lite
    implementation(libs.litert)

    // OkHttp
    implementation(platform(libs.okhttp.bom))
    implementation(libs.okhttp)

    // Image loading
    implementation(libs.coil.compose)
    implementation(libs.accompanist.drawablepainter)

    // Permission Flow
    implementation(libs.permission.flow.android)
    implementation(libs.permission.flow.compose)

    // Miscellaneous
    implementation(libs.unbescape)
    implementation(libs.jsoup)

    // Used by skills
    implementation(libs.exp4j)

    // Testing
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotest.property)
    androidTestImplementation(libs.test.runner)
    androidTestImplementation(libs.test.rules)
    androidTestImplementation(libs.test.ui.automator)
}

// Required to avoid NoClassDefFoundError for ActivityInvoker during androidTest
configurations.configureEach {
    resolutionStrategy {
        force(libs.test.core)
    }
}

fun gitBranch(): String {
    return Git.open(rootDir).use { it.repository.branch }
}

