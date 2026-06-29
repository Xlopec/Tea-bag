/*
 * MIT License
 *
 * Copyright (c) 2026. Maksym Oliinyk.
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

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
    `kotlin-dsl-precompiled-script-plugins`
}

group = "io.github.xlopec"
version = "SNAPSHOT"

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

tasks.withType<JavaCompile>().configureEach {
    targetCompatibility = "17"
    sourceCompatibility = "17"
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

dependencies {
    implementation(libs.convention.kotlin)
    implementation(libs.convention.dokka)
    implementation(libs.convention.serializtion)
    implementation(libs.convention.agp)
    implementation(libs.convention.sqldelight)
    implementation(libs.convention.compose.plugin)
    implementation(libs.convention.compose.compiler)
    implementation(libs.convention.vanniktech.publish)
    implementation(libs.convention.detekt)
    implementation(libs.convention.versions)

    testImplementation(libs.junit)
    testImplementation(libs.junit.runner)
}
