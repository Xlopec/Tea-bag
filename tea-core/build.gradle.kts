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
import Libraries.kotlinStdLib
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

/**
 * in case of shit, add this back to xcode -> .xcodeproj -> Build Phases -> Run Script
 * cd "$SRCROOT/.."
./gradlew :tea-core:packForXCode :shared-app-lib:packForXCode -PXCODE_CONFIGURATION=${CONFIGURATION}
 */

plugins {
    `maven-publish`
    signing
    id("org.jetbrains.dokka")
    kotlin("multiplatform")
    kotlin("native.cocoapods")
}

version = "1.0.0"

kotlin {

    explicitApi()

    jvm {
        withJava()
    }

    val iosTarget: (String, KotlinNativeTarget.() -> Unit) -> KotlinNativeTarget =
        if (System.getenv("SDK_NAME")?.startsWith("iphoneos") == true)
            ::iosArm64
        else
            ::iosX64

    iosTarget("ios") {}

    cocoapods {
        summary = "Tea core library"
        homepage = "Link to the Tea library Module homepage"
        ios.deploymentTarget = "14.0"
        frameworkName = "TeaCore"
        podfile = project.file("../iosApp/Podfile")
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(coroutinesCore + "-native-mt")
                implementation(kotlinStdLib)
            }
        }

        val commonTest by getting

        val jvmMain by getting

        val jvmTest by getting {
            dependencies {
                implementation(project(":tea-test"))
            }
        }

        val iosMain by getting {
            dependencies {
                //implementation(kotlinStdLib)
                //dependsOn(commonMain)
                //org.jetbrains.kotlinx:kotlinx-coroutines-core-iosx64:1.5.0-native-mt'
                //api("org.jetbrains.kotlinx:kotlinx-coroutines-core-iosx64:1.5.0")
            }
        }
        val iosTest by getting
    }
}