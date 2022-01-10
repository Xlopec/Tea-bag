import org.jetbrains.kotlin.gradle.plugin.LanguageSettingsBuilder

plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
}

version = libraryVersion.toVersionName()
group = "io.github.xlopec"

kotlin {
    explicitApi()

    jvm {
        withJava()

        testRuns["test"].executionTask.configure {
            useJUnit()
        }
    }

    ios()

    sourceSets {

        all {
            languageSettings {
                optIn(
                    "kotlinx.coroutines.ExperimentalCoroutinesApi",
                    "kotlin.RequiresOptIn",
                    "kotlinx.coroutines.InternalCoroutinesApi",
                    "kotlinx.coroutines.FlowPreview",
                    "kotlin.ExperimentalStdlibApi",
                    "com.oliynick.max.tea.core.UnstableApi"
                )
            }
        }
    }
}

fun LanguageSettingsBuilder.optIn(
    vararg annotationNames: String
) = annotationNames.forEach(::optIn)