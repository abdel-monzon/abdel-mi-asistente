import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.com.android.library)
    alias(libs.plugins.org.jetbrains.kotlin.android)
    alias(libs.plugins.com.google.devtools.ksp)
    alias(libs.plugins.com.google.dagger.hilt.android)
    alias(libs.plugins.dicio.sentences.compiler.plugin)
    alias(libs.plugins.dicio.unicode.cldr.plugin)
}

android {
    namespace = "org.stypox.dicio.skill"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.java.get())
        targetCompatibility = JavaVersion.toVersion(libs.versions.java.get())
    }

    kotlin {
        compilerOptions {
            jvmTarget = JvmTarget.fromTarget(libs.versions.java.get())
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
    
    kotlinOptions {
        freeCompilerArgs += listOf(
            "-Xjvm-default=all",
            "-opt-in=kotlin.RequiresOptIn"
        )
    }
}

dependencies {
    implementation(libs.appcompat)

    implementation(libs.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    androidTestImplementation(platform(libs.compose.bom))
    debugImplementation(libs.debug.compose.ui.tooling)
    debugImplementation(libs.debug.compose.ui.test.manifest)

    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    androidTestImplementation(libs.hilt.android.testing)

    implementation(libs.dicio.sentences.compiler)
    implementation(libs.kotlin.serialization)

    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    androidTestImplementation(libs.test.runner)
    androidTestImplementation(libs.test.rules)
}

// ... (todo el contenido anterior del archivo se mantiene igual) ...

// 🔽 CONFIGURACIÓN del plugin SENTENCES-COMPILER (ya la tienes)
tasks.withType<org.stypox.dicio.sentencesCompilerPlugin.SentencesCompilerTask> {
    inputDir.set(project.rootProject.file("app/src/main/sentences"))
    
    val sentencesDir = inputDir.get().asFile
    if (!sentencesDir.exists()) {
        println("❌ ERROR: Directorio de sentences no existe")
        sentencesDir.mkdirs()
    } else {
        println("✅ Directorio de sentences encontrado")
    }
}

// 🔽 CONFIGURACIÓN CORREGIDA para el plugin UNICODE-CLDR (NUEVA VERSIÓN)
tasks.withType<org.stypox.dicio.unicodeCldrPlugin.UnicodeCldrLanguagesTask> {
    // 1. Configurar el commit de Git
    unicodeCldrGitCommit = libs.versions.unicodeCldrGitCommit.get()
    
    // 2. 🔽 CORRECCIÓN: Apuntar al archivo proto en la app
    dicioLanguagesFile.set(project.rootProject.file("app/src/main/proto/language.proto"))
    
    // 3. Verificación
    val protoFile = dicioLanguagesFile.get().asFile
    if (!protoFile.exists()) {
        println("❌ ERROR: Archivo proto no encontrado: ${protoFile.absolutePath}")
        // Puedes crear un archivo proto básico temporal si es necesario:
        /*
        protoFile.parentFile.mkdirs()
        protoFile.writeText("""
            // Archivo language.proto generado temporalmente
            syntax = "proto3";
            
            package org.stypox.dicio.language;
            
            // Definiciones de lenguaje...
        """.trimIndent())
        println("⚠️  Archivo proto temporal creado")
        */
    } else {
        println("✅ Archivo proto encontrado: ${protoFile.name}")
    }
}
