import gradle.kotlin.dsl.accessors._4912aece6b5b5be71917a8f507c3c7eb.archives
import gradle.kotlin.dsl.accessors._4912aece6b5b5be71917a8f507c3c7eb.classes
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.dokka.gradle.GradleDokkaSourceSetBuilder
import org.jetbrains.kotlin.gradle.plugin.LanguageSettingsBuilder
import java.net.URL

plugins {
    `maven-publish`
    signing
    id("org.jetbrains.dokka")
    kotlin("multiplatform")
    kotlin("native.cocoapods")
}

version = "1.0.0"

kotlin {
    explicitApi()

    jvm {
        withJava()

        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
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

val sourcesJar by tasks.named("sourcesJar")

val javadocJar by tasks.registering(Jar::class) {
    dependsOn(tasks.named("dokkaJavadoc"))
    archiveClassifier.set("javadoc")
    from("$buildDir/javadoc")
}

val copyArtifacts by tasks.registering(Copy::class) {
    group = "release"
    description = "Copies artifacts to the 'artifacts' from project's 'libs' dir for CI"
    from("$buildDir/libs/")
    into("${rootProject.buildDir}/artifacts/${project.name}")
}

tasks.withType<DokkaTask>().configureEach {

    outputDirectory.set(buildDir.resolve("documentation"))

    dokkaSourceSets {

        configureEach {
            reportUndocumented.set(true)
            includeNonPublic.set(false)
            skipEmptyPackages.set(true)
        }

        named("commonMain") {
            linkSourcesForSourceSet("commonMain")
        }

        named("jvmMain") {
            linkSourcesForSourceSet("jvmMain")
        }

        named("iosMain") {
            linkSourcesForSourceSet("iosMain")
        }
    }
}

artifacts {
    archives(sourcesJar)
    //archives(javadocJar)
}

fun LanguageSettingsBuilder.optIn(
    vararg annotationNames: String
) = annotationNames.forEach(::optIn)

fun GradleDokkaSourceSetBuilder.linkSourcesForSourceSet(
    name: String
) = sourceLink {
    localDirectory.set(file("src/$name/kotlin"))
    remoteUrl.set(URL("https://github.com/Xlopec/Tea-bag/tree/$branchOrDefault/${project.name}/src/$name/kotlin"))
    remoteLineSuffix.set("#L")
}