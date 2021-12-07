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

import Libraries.composeRuntime
import Libraries.coroutinesBom
import Libraries.coroutinesCore
import Libraries.gson
import Libraries.immutableCollections
import Libraries.kotlinStdLib
import Libraries.ktorClientCio
import Libraries.ktorClientCore
import Libraries.ktorClientGson
import Libraries.ktorClientIos
import Libraries.ktorClientJson
import Libraries.ktorClientLogging
import Libraries.ktorClientSerialization

plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("com.android.library")
    id("com.squareup.sqldelight")
    kotlin("plugin.serialization")
}

version = "1.0.0"

kotlin {

    android()

    ios()

    cocoapods {
        summary = "Shared app lib with common code"
        homepage = "Link to the Shared Module homepage"
        ios.deploymentTarget = "14.1"
        frameworkName = "SharedAppLib"
        podfile = project.file("../iosApp/Podfile")
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(immutableCollections)
                api(project.enforcedPlatform(coroutinesBom))
                api(coroutinesCore)
                api(project(":tea-core"))
                implementation(kotlinStdLib)
                implementation(ktorClientCore)
                implementation(ktorClientLogging)
                implementation(ktorClientJson)
                implementation(ktorClientSerialization)
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.3.1")
                implementation("com.russhwolf:multiplatform-settings:0.8.1")

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
                implementation(ktorClientCio)
                implementation(ktorClientGson)
                implementation(gson)
                implementation(composeRuntime)
                implementation("com.squareup.sqldelight:android-driver:1.5.2")
                api(project(":tea-time-travel"))
                api(project(":tea-time-travel-adapter-gson"))
            }
        }
        val androidTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation("junit:junit:4.13.2")
            }
        }
        val iosMain by getting {
            dependencies {
                implementation(ktorClientIos)
                implementation("com.squareup.sqldelight:native-driver:1.5.2")
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
        packageName = "com.oliynick.max.reader.app.storage"
    }
}