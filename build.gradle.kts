// build.gradle.kts (nivel raíz)
plugins {
    id("com.android.application") version "8.5.2" apply false
    id("org.jetbrains.kotlin.android") version "2.1.0" apply false
    id("com.google.devtools.ksp") version "2.0.21-1.0.26" apply false
    id("io.kotzilla.kotzilla-plugin") version "1.3.0" apply false
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}