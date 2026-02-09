plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }

    jvm()

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.stdlib)
                implementation(libs.coroutines.core)
                implementation(project(":tea-core"))
                implementation(compose.runtime)
            }
        }
    }
}