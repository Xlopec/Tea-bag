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

plugins {
    kotlin("jvm")
    id("org.jetbrains.dokka")
    id("io.gitlab.arturbosch.detekt")
}

group = "com.github.Xlopec"
version = "0.0.2-alpha1"

repositories {
    mavenLocal()
    jcenter()
}


dependencies {

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(Libraries.kotlinStdLib)//"org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin_version"
    implementation(Libraries.kotlinReflect) //"org.jetbrains.kotlin:kotlin-reflect:$kotlin_version"

    testImplementation(project(path = ":elm-core-test", configuration = "default"))

    api(Libraries.coroutinesCore)//"org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines_version"
}
