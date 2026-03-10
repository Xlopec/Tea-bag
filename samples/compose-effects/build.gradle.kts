plugins {
    id("multiplatform-convention")
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    compilerOptions {
        optIn.addAll(
            "io.github.xlopec.tea.core.ExperimentalTeaApi",
            "kotlin.time.ExperimentalTime",
            "androidx.compose.runtime.InternalComposeApi"
        )
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
                implementation(project(":tea-compose"))
            }
        }
    }
}
