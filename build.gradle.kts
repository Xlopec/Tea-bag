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

import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.dokka.gradle.DokkaTask
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
    kotlin()
    detekt()
    dokka()
    `maven-publish`
}

allprojects {
    apply {
        plugin("io.gitlab.arturbosch.detekt")
    }
}

allprojects
    .filterNot { project -> project.name == "app" }
    .forEachApplying {

        apply {
            plugin("maven-publish")
            plugin("org.jetbrains.kotlin.jvm")
            plugin("org.jetbrains.dokka")
        }

        val dokka by tasks.getting(DokkaTask::class) {
            outputFormat = "html"
            outputDirectory = "$buildDir/javadoc"

            configuration {

                moduleName = name

                externalDocumentationLink {
                    url = URL("https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/")
                    url = URL("https://javadoc.io/doc/com.google.code.gson/gson/latest/com.google.gson/")
                }

                jdkVersion = JavaVersion.VERSION_1_8.majorVersionInt
            }

        }

        val sourcesJar by tasks.creating(Jar::class) {
            dependsOn(tasks.classes)
            archiveClassifier.set("sources")
            from(sourceSets.main.get().allSource)
        }

        val javadocJar by tasks.creating(Jar::class) {
            dependsOn(dokka)
            archiveClassifier.set("javadoc")
            from("$buildDir/javadoc")
        }

        publishing {

            publications {
                create<MavenPublication>(name) {
                    from(components["java"])
                    artifact(sourcesJar)
                    artifact(javadocJar)

                    groupId = "com.github.Xlopec"
                    artifactId = name
                    version = "0.0.2-alpha1"
                }
            }

            repositories {
                mavenLocal()
            }
        }

        artifacts {
            archives(sourcesJar)
            archives(javadocJar)
        }

        detekt {
            toolVersion = BuildPlugins.Versions.detektVersion
            input = files("src/main/kotlin", "src/main/java")
        }
    }

val detektAll by tasks.registering(Detekt::class) {
    description = "Runs analysis task over whole code"
    debug = true
    parallel = true
    ignoreFailures = false
    disableDefaultRuleSets = false
    buildUponDefaultConfig = true
    setSource(files(projectDir))
    config.setFrom(files("$rootDir/detekt/detekt-config.yml"))
    baseline.set(file("$rootDir/detekt/detekt-baseline.xml"))

    include("**/*.kt", "**/*.kts")
    exclude("resources/", "build/")

    reports {
        xml.enabled = false
        txt.enabled = false
        html.enabled = true
    }
}

val detektFormat by tasks.creating(Detekt::class) {
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

    config.setFrom(files("$rootDir/detekt/detekt-config.yml"))
}

allprojects {
    repositories {
        mavenLocal()
        google()
        jcenter()
    }

    tasks.withType<KotlinCompile>().all {
        kotlinOptions {
            // disables warning about usage of experimental Kotlin features
            @Suppress("SuspiciousCollectionReassignment")
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
