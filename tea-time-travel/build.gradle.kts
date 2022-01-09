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

import Libraries.kotlinStdLib
import Libraries.kotlinStdLibBom
import Libraries.ktorClientCio
import Libraries.ktorClientCore
import Libraries.ktorClientWebsockets
import TestLibraries.ktorMockJvm

plugins {
    `published-multiplatform-library`
}

kotlin {

    cocoapods {
        summary = "Tea time travel library"
        homepage = "Link to the Tea library Module homepage"
        ios.deploymentTarget = "14.0"
        frameworkName = "TeaTimeTravel"
        podfile = project.file("../iosApp/Podfile")
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":tea-core"))
                api(project(":tea-time-travel-protocol"))
                api(project(":tea-time-travel-protocol"))

                implementation(kotlinStdLib)
                implementation(project.platform(kotlinStdLibBom))
                implementation(ktorClientWebsockets)
                implementation(ktorClientCore)
            }
        }

        val commonTest by getting

        val jvmMain by getting {
            dependencies {
                implementation(ktorClientCio)
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation("junit:junit:4.13.2")
                implementation(project(":tea-test"))
                implementation(project(":tea-time-travel-adapter-gson"))
                implementation(ktorMockJvm)
            }
        }

        val iosMain by getting

        val iosTest by getting
    }
}