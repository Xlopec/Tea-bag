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

repositories {
    mavenCentral()
    maven("https://plugins.gradle.org/m2/")
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
    mavenLocal()
    maven("https://maven.pkg.jetbrains.space/public/p/kotlinx-coroutines/maven")
    maven("https://maven.pkg.jetbrains.space/public/p/ktor/eap")
}

plugins {
    `kotlin-dsl`
    `kotlin-dsl-precompiled-script-plugins`
}

afterEvaluate {
    tasks.withType<Test>().all {

        val buildDir = File(rootProject.rootDir.parentFile, "build")

        reports {
            html.destination =
                File("${buildDir}/junit-reports/${project.name}/html")
            junitXml.destination =
                File("${buildDir}/junit-reports/${project.name}/xml")
        }
    }
}

dependencies {
    implementation("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.5")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.0")
    implementation("com.android.tools.build:gradle:7.0.3")
    implementation("org.jetbrains.intellij.plugins:gradle-intellij-plugin:1.1.4")
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:1.4.32")
    implementation("org.jetbrains.kotlin:kotlin-serialization:1.5.21")
    implementation("com.squareup.sqldelight:gradle-plugin:1.5.2")
    implementation("org.jetbrains.compose:compose-gradle-plugin:1.0.0")
    implementation("com.github.ben-manes:gradle-versions-plugin:0.39.0")

    testImplementation("junit:junit:4.13.1")
    testImplementation("io.kotlintest:kotlintest-runner-junit4:3.4.2")
}
