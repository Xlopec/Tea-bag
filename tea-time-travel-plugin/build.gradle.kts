/*
 * Copyright (C) 2021. Maksym Oliinyk.
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


import Libraries.coroutinesCore
import Libraries.coroutinesSwing
import Libraries.immutableCollections
import Libraries.kotlinStdLib
import Libraries.ktorServerCore
import Libraries.ktorServerNetty
import Libraries.ktorServerWebsockets
import Libraries.logback
import TestLibraries.ktorServerTests
import org.jetbrains.intellij.tasks.PatchPluginXmlTask
import org.jetbrains.intellij.tasks.PublishTask

intellij {
    version = "2020.3"
    setPlugins("com.intellij.java")
}

tasks.named<PatchPluginXmlTask>("patchPluginXml") {
    setVersion(versionName())
}

tasks.named<PublishTask>("publishPlugin") {
    token(System.getenv("PUBLISH_PLUGIN_TOKEN"))
    channels(*pluginReleaseChannels())
}

sourceSets {
    main {
        resources {
            srcDir("resources")
        }
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(project(":tea-core"))
    implementation(project(":tea-time-travel-protocol"))
    implementation(project(":tea-time-travel-adapter-gson"))

    implementation(kotlinStdLib)

    implementation("ch.qos.logback:logback-classic:$logback")
    implementation(ktorServerCore)
    implementation(ktorServerNetty)
    implementation(ktorServerWebsockets)
    implementation(coroutinesCore)
    implementation(coroutinesSwing)
    implementation(immutableCollections)

    testImplementation(ktorServerTests)
    testImplementation(project(":tea-test"))
}