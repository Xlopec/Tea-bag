/*
 * MIT License
 *
 * Copyright (c) 2022. Maksym Oliinyk.
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
    google()
    mavenLocal()

    maven {
        setUrl("https://maven.pkg.jetbrains.space/public/p/compose/dev/")
    }
}

plugins {
    `kotlin-dsl`
    `kotlin-dsl-precompiled-script-plugins`
}

afterEvaluate {
    tasks.withType<Test>().configureEach {
        val buildDir = File(File(File(rootProject.rootDir.parentFile, "build"), "junit-reports"), project.name)

        description = "$description Also copies test reports to $buildDir"

        reports {
            html.outputLocation.set(File(buildDir, "html"))
            junitXml.outputLocation.set(File(buildDir, "xml"))
        }
    }
}

//noinspection UseTomlInstead
dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.22")
    implementation("com.android.tools.build:gradle:8.2.0")
    implementation("org.jetbrains.intellij.plugins:gradle-intellij-plugin:1.17.2")
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:1.9.20")
    implementation("org.jetbrains.kotlin:kotlin-serialization:1.9.22")
    implementation("com.squareup.sqldelight:gradle-plugin:1.5.5")
    implementation("org.jetbrains.compose:compose-gradle-plugin:1.6.0")
    implementation("com.github.ben-manes:gradle-versions-plugin:0.51.0")
    implementation("io.github.gradle-nexus:publish-plugin:1.3.0")
    implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.23.5")

    testImplementation("junit:junit:4.13.2")
    // used for tests under buildSrc directory
    testImplementation("io.kotlintest:kotlintest-runner-junit4:3.4.2")
}
