plugins {
    kotlin("multiplatform")
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
        commonMain {
            dependencies {
                implementation(libs.stdlib)
                implementation(libs.coroutines.core)
                implementation(project(":tea-core"))
            }
        }
    }
}
