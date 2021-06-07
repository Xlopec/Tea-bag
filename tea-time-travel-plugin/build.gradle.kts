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


import Libraries.coroutinesCore
import Libraries.coroutinesSwing
import Libraries.immutableCollections
import Libraries.kotlinStdLib
import Libraries.kotlinStdLibReflect
import Libraries.ktorServerCore
import Libraries.ktorServerNetty
import Libraries.ktorServerWebsockets
import Libraries.logback
import TestLibraries.ktorServerTests
import org.jetbrains.intellij.tasks.PatchPluginXmlTask
import org.jetbrains.intellij.tasks.PublishTask

plugins {
    `maven-publish`
    `java-library`
    kotlin("jvm")
    intellij()
}

intellij {
    version = "2020.3"
    setPlugins("com.intellij.java")
}

tasks.named<PatchPluginXmlTask>("patchPluginXml") {
    setVersion(versionName)
}

tasks.named<PublishTask>("publishPlugin") {
    token(ciVariable("PUBLISH_PLUGIN_TOKEN"))
    channels(*pluginReleaseChannels)
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
    implementation(kotlinStdLibReflect)

    implementation(logback)
    implementation(ktorServerCore)
    implementation(ktorServerNetty)
    implementation(ktorServerWebsockets)
    implementation(coroutinesCore)
    implementation(coroutinesSwing)
    implementation(immutableCollections)

    testImplementation(ktorServerTests)
    testImplementation(project(":tea-test"))
}
