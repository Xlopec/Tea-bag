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


import Libraries.atomicfu
import Libraries.coroutinesCore
import Libraries.coroutinesTest
import Libraries.kotlinStdLib
import TestLibraries.kotlinTest

plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
}

version = "1.0.0"

kotlin {

    jvm {
        withJava()

        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    ios()

    cocoapods {
        summary = "Tea test library"
        homepage = "Link to the Tea test library Module homepage"
        ios.deploymentTarget = "14.0"
        frameworkName = "TeaTest"
        podfile = project.file("../iosApp/Podfile")
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(kotlinStdLib)
                api(atomicfu)
                api(coroutinesCore)
                api(coroutinesTest)
                api(kotlinTest)
                api("org.jetbrains.kotlin:kotlin-test-annotations-common")
                implementation(project(":tea-core"))
            }
        }

        val commonTest by getting {
            dependencies {

            }
        }

        val iosMain by getting

        val iosTest by getting

        val jvmMain by getting

        val jvmTest by getting
    }
}
