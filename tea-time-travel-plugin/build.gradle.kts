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

buildscript {

    repositories {
        jcenter()
        google()
        mavenCentral()
        maven {
            setUrl("https://plugins.gradle.org/m2/")
        }
    }

    dependencies {
        classpath(BuildPlugins.kotlinGradlePlugin)
        classpath("gradle.plugin.org.jetbrains.intellij.plugins:gradle-intellij-plugin:0.4.16")
    }
}

plugins {
    id("org.jetbrains.intellij") version "0.4.16"
}

group = "com.github.Xlopec.time.travel"
version = "0.0.1"

sourceSets {
    main {
        resources {
            srcDir("resources")
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

configurations.all {
    resolutionStrategy.force(Libraries.coroutinesCore)
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(project(path = ":tea-core", configuration = "default"))
    implementation(project(path = ":tea-time-travel-protocol", configuration = "default"))
    implementation(project(path = ":tea-time-travel-adapter-gson", configuration = "default"))

    implementation(Libraries.kotlinStdLib)

    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation(Libraries.ktorServerCore)
    implementation(Libraries.ktorServerNetty)
    implementation(Libraries.ktorServerWebsockets)
    implementation(Libraries.coroutinesCore)
    implementation(Libraries.coroutinesSwing)
    implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3")

    testImplementation(TestLibraries.ktorServerTests)
    testImplementation(project(path = ":tea-test", configuration = "default"))
}

intellij {
    version = "2019.1"
}