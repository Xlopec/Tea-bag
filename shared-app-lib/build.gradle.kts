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
import Libraries.kotlinStdLibBom
import Libraries.ktorClientCio
import Libraries.ktorClientCore
import Libraries.ktorClientGson
import Libraries.ktorClientIos
import Libraries.ktorClientJson
import Libraries.ktorClientLogging
import Libraries.ktorClientSerialization
import Libraries.sqlDelightAndroidDriver
import Libraries.sqlDelightNativeDriver

plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("com.android.library")
    id("com.squareup.sqldelight")
    kotlin("plugin.serialization")
}

version = "1.0.0"

tasks.withType<Test>().whenTaskAdded {
    onlyIf { isCiEnv }
}

kotlin {

    android()

    iosX64()
    iosArm64()

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
                api(project(":shared-entities"))
                api(immutableCollections)
                api(project.platform(coroutinesBom))
                api(coroutinesCore)
                api(project(":tea-core"))
                implementation(kotlinStdLib)
                implementation(project.platform(kotlinStdLibBom))
                implementation(ktorClientCore)
                implementation(ktorClientLogging)
                implementation(ktorClientJson)
                implementation(ktorClientSerialization)
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
                implementation(ktorClientCio)
                implementation(ktorClientGson)
                implementation(gson)
                implementation(composeRuntime)
                implementation(sqlDelightAndroidDriver)
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

        val iosX64Main by getting
        val iosArm64Main by getting
        //val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            //iosSimulatorArm64Main.dependsOn(this)

            dependencies {
                implementation(ktorClientIos)
                implementation(sqlDelightNativeDriver)
            }
        }
        val iosX64Test by getting
        val iosArm64Test by getting
        //val iosSimulatorArm64Test by getting
        val iosTest by creating {
            dependsOn(commonTest)
            iosX64Test.dependsOn(this)
            iosArm64Test.dependsOn(this)
            //iosSimulatorArm64Test.dependsOn(this)
        }
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