/*
 * MIT License
 *
 * Copyright (c) 2021. Maksym Oliinyk.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */


import Libraries.coroutinesBom
import Libraries.coroutinesCore
import Libraries.coroutinesSwing
import Libraries.immutableCollections
import Libraries.kotlinStdLib
import Libraries.kotlinStdLibBom
import Libraries.kotlinStdLibReflect
import Libraries.ktorServerCore
import Libraries.ktorServerNetty
import Libraries.ktorServerWebsockets
import Libraries.logback
import TestLibraries.ktorServerTests
import org.jetbrains.compose.compose
import org.jetbrains.intellij.tasks.PatchPluginXmlTask
import org.jetbrains.intellij.tasks.PublishPluginTask

plugins {
    `maven-publish`
    `java-library`
    kotlin("jvm")
    intellij()
    id("org.jetbrains.compose")
}

repositories {
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

intellij {
    version.set("2020.3")
    plugins.add("com.intellij.java")
}

tasks.named<PatchPluginXmlTask>("patchPluginXml") {
    setVersion(libraryVersion.toVersionName())
}

tasks.named<PublishPluginTask>("publishPlugin") {
    token.set(ciVariable("PUBLISH_PLUGIN_TOKEN"))
    channels.set(pluginReleaseChannels)
}

val copyArtifacts by tasks.registering(Copy::class) {
    group = "release"
    description = "Copies artifacts to the 'artifacts' from project's 'libs' dir for CI"
    from("$buildDir/libs/", "$buildDir/distributions/")
    into("${rootProject.buildDir}/artifacts/${project.name}")
}

val releasePlugin by tasks.creating(Task::class) {
    group = "release"
    description = "Runs build tasks, assembles all the necessary artifacts and publishes them"
    dependsOn("publishPlugin")
    finalizedBy(copyArtifacts)
}

val ciTests by tasks.registering(Test::class) {
    group = "verification"
    description = "Prepares and runs tests relevant for CI build"

    dependsOn(tasks.test.get())
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().all {
    kotlinOptions {
        jvmTarget = "11"

        freeCompilerArgs += listOf(
            "-Xuse-experimental=kotlin.ExperimentalUnsignedTypes",
            "-Xuse-experimental=kotlin.Experimental",
            "-Xuse-experimental=kotlin.contracts.ExperimentalContracts",
            "-Xuse-experimental=kotlinx.coroutines.FlowPreview",
            "-Xuse-experimental=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-Xuse-experimental=io.ktor.util.KtorExperimentalAPI",
            "-Xuse-experimental=kotlin.ExperimentalStdlibApi",
            "-XXLanguage:+NewInference",
            "-XXLanguage:+InlineClasses"
        )
    }
}

sourceSets {
    main {
        resources {
            srcDir("resources")
        }
    }
}

dependencies {

    implementation(project(":tea-core"))
    implementation(project(":tea-time-travel-protocol"))
    implementation(project(":tea-time-travel-adapter-gson"))

    implementation(kotlinStdLib)
    implementation(project.enforcedPlatform(kotlinStdLibBom))
    implementation(kotlinStdLibReflect)

    implementation(compose.desktop.currentOs)
    implementation(logback)

    implementation(ktorServerCore)
    implementation(ktorServerNetty)
    implementation(ktorServerWebsockets)
    implementation(project.enforcedPlatform(coroutinesBom))
    implementation(coroutinesCore)
    implementation(coroutinesSwing)
    implementation(immutableCollections)

    testImplementation(ktorServerTests)
    testImplementation(project(":tea-test"))
}
