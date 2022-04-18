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

plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("com.android.library")
    id("com.squareup.sqldelight")
    kotlin("plugin.serialization")
}

version = "1.0.0"

tasks.withType<Test>().whenTaskAdded {
    onlyIf { !isCiEnv }
}

kotlin {

    optIn("kotlinx.serialization.ExperimentalSerializationApi", "io.github.xlopec.tea.core.ExperimentalTeaApi")

    android()

    ios()

    cocoapods {
        summary = "Shared app lib with common code"
        homepage = "Link to the Shared Module homepage"
        ios.deploymentTarget = "14.1"
        framework {
            baseName = "SharedAppLib"
        }
        podfile = project.file("../iosApp/Podfile")
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":tea-core"))
                api(project(":tea-data"))
                api(libs.collections.immutable)
                api(libs.coroutines.core)
                implementation(libs.stdlib)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.logging)

                implementation(libs.ktor.client.negotiation)
                implementation(libs.ktor.serialization.json)

                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.3.2")
                implementation("com.russhwolf:multiplatform-settings-no-arg:0.8.1")
                implementation("com.squareup.sqldelight:runtime:1.5.3")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.ktor.client.cio)
                implementation(libs.ktor.client.gson)
                implementation(libs.gson)
                implementation(libs.compose.runtime)
                implementation(libs.sqldelight.driver.android)
                api(project(":tea-time-travel"))
                api(project(":tea-time-travel-adapter-gson"))
            }
        }
        val androidTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation(libs.junit)
            }
        }
        val iosMain by getting {
            dependencies {
                implementation(libs.ktor.client.ios)
                implementation(libs.sqldelight.driver.native)
            }
        }
        val iosTest by getting
    }
}

android {
    compileSdk = 31
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 21
        targetSdk = 31
    }
}

sqldelight {
    database("AppDatabase") {
        packageName = "io.github.xlopec.reader.app.storage"
    }
}