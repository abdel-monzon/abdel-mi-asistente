import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.com.android.library)
    alias(libs.plugins.org.jetbrains.kotlin.android)
    alias(libs.plugins.com.google.devtools.ksp)
    alias(libs.plugins.com.google.dagger.hilt.android)
    alias(libs.plugins.dicio.sentences.compiler.plugin)
    alias(libs.plugins.dicio.unicode.cldr.plugin)
    // 🔽 NO incluir plugins complementarios de Kotlin aquí
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
        compose = true  // 🔽 Habilita Compose aquí (sin plugin aparte)
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
    // Dependencias básicas de Android
    implementation(libs.appcompat)

    // Dependencias de Compose
    implementation(libs.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    androidTestImplementation(platform(libs.compose.bom))
    debugImplementation(libs.debug.compose.ui.tooling)
    debugImplementation(libs.debug.compose.ui.test.manifest)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    androidTestImplementation(libs.hilt.android.testing)

    // Dependencias específicas del módulo skill
    implementation(libs.dicio.sentences.compiler)
    implementation(libs.kotlin.serialization)

    // Testing
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    androidTestImplementation(libs.test.runner)
    androidTestImplementation(libs.test.rules)
}
