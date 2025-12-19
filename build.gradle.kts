// 📄 Archivo: build.gradle.kts (en la RAIZ del proyecto)
plugins {
    id("org.jetbrains.kotlin.android") version "2.0.0" apply false
    // Agrega esta línea para resolver el conflicto de parcelize
    id("org.jetbrains.kotlin.plugin.parcelize") version "2.0.0" apply false
}
