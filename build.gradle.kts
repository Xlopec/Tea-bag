/*
 * Copyright (C) 2019 Maksym Oliinyk.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URL

// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {

    repositories {
        mavenLocal()
        google()
        jcenter()
        maven { setUrl("https://jitpack.io") }
        maven { setUrl("https://plugins.gradle.org/m2/") }
    }

    dependencies {
        classpath(BuildPlugins.kotlinGradlePlugin)
        classpath(BuildPlugins.androidGradlePlugin)
        classpath(BuildPlugins.androidMaven)
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

plugins {
    kotlin("jvm") version "1.3.61"
    id("io.gitlab.arturbosch.detekt") version "1.0.0-RC16"
    id("org.jetbrains.dokka") version "0.10.1"
}

detekt {
    toolVersion = "1.0.0-RC16"
    input = files("src/main/kotlin", "src/main/java")
    filters = ".*/resources/.*,.*/build/.*"
    baseline = file("$rootDir/detekt/detekt-baseline.xml")
    config = files("$rootDir/detekt/detekt-config.yml")
}

val dokka by tasks.getting(org.jetbrains.dokka.gradle.DokkaTask::class) {
    outputFormat = "html"
    outputDirectory = "$buildDir/javadoc"

    subProjects = listOf(
        "elm-core-component",
        "elm-core-component-debug",
        "elm-time-travel-adapter-gson",
        "elm-time-travel-protocol"
    )

    configuration {

        moduleName = "Tea Core"

        externalDocumentationLink {
            url = URL("https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/")
            url = URL("https://javadoc.io/doc/com.google.code.gson/gson/latest/com.google.gson/")
        }

        jdkVersion = 8
    }

}

val detektFormat by tasks.creating(io.gitlab.arturbosch.detekt.Detekt::class) {
    parallel = true
    autoCorrect = true
    buildUponDefaultConfig = true
    failFast = false
    ignoreFailures = false
    setSource(files(projectDir))

    include("**/*.kt")
    include("**/*.kts")
    exclude("**/resources/**")
    exclude("**/build/**")

    config = files("$rootDir/detekt/detekt-config.yml")
}

val javadocJar by tasks.creating(org.gradle.jvm.tasks.Jar::class) {
    classifier = "javadoc"
    from("$buildDir/javadoc")
    dependsOn(dokka)
}

val sourcesJar by tasks.creating(Jar::class) {
    dependsOn(JavaPlugin.CLASSES_TASK_NAME)
    classifier = "sources"
    from(sourceSets["main"].allSource)
}

artifacts {
    add("archives", sourcesJar)
    add("archives", javadocJar)
}

allprojects {
    repositories {
        mavenLocal()
        google()
        jcenter()
    }
}

project.afterEvaluate {

    allprojects {

        tasks.withType<KotlinCompile>().all {
            kotlinOptions {
                // disables warning about usage of experimental Kotlin features
                freeCompilerArgs += listOf(
                    "-Xuse-experimental=kotlin.ExperimentalUnsignedTypes",
                    "-Xuse-experimental=kotlin.Experimental",
                    "-Xuse-experimental=kotlin.contracts.ExperimentalContracts",
                    "-Xuse-experimental=kotlinx.coroutines.FlowPreview",
                    "-Xuse-experimental=kotlinx.coroutines.ExperimentalCoroutinesApi",
                    "-Xuse-experimental=io.ktor.util.KtorExperimentalAPI",
                    "-XXLanguage:+NewInference",
                    "-XXLanguage:+InlineClasses"
                )
            }
        }
    }
}