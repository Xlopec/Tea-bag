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
import Libraries.ktorClientCio
import Libraries.ktorClientWebsockets
import TestLibraries.ktorMockJvm

plugins {
    `maven-publish`
    signing
    id("org.jetbrains.dokka")
    kotlin("multiplatform")
}

kotlin {

    explicitApi()

    jvm {
        withJava()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":tea-core"))
                api(project(":tea-time-travel-protocol"))

                implementation(kotlinStdLib)

                implementation(ktorClientWebsockets)
                implementation(ktorClientCio)
            }
        }

        val commonTest by getting

        val jvmMain by getting

        val jvmTest by getting {
            dependencies {
                implementation(project(":tea-test"))
                implementation(project(":tea-time-travel-adapter-gson"))
                implementation(ktorMockJvm)
            }
        }
    }
}


/*dependencies {

    implementation(project(":tea-core"))
    api(project(":tea-time-travel-protocol"))

    implementation(kotlinStdLib)

    implementation(ktorClientWebsockets)
    implementation(ktorClientCio)

    testImplementation(project(":tea-test"))
    testImplementation(project(":tea-time-travel-adapter-gson"))
    testImplementation(ktorMockJvm)

}*/
