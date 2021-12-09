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

import Libraries.coroutinesBom
import Libraries.coroutinesCore
import Libraries.kotlinStdLib
import Libraries.kotlinStdLibBom
import TestLibraries.kotlinTest

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

        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    ios()

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
                api(project.enforcedPlatform(coroutinesBom))
                api(coroutinesCore)
                implementation(kotlinStdLib)
                implementation(project.enforcedPlatform(kotlinStdLibBom))
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlinTest)
                implementation(project(":tea-test"))
            }
        }

        val jvmMain by getting

        val jvmTest by getting

        val iosMain by getting

        val iosTest by getting
    }
}