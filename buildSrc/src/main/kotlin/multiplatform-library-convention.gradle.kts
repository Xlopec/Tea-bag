import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTargetWithSimulatorTests

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
}

version = libraryVersion.toVersionName()
group = "io.github.xlopec"

kotlin {
    explicitApi()

    jvm {
        withJava()

        testRuns["test"].executionTask.configure {
            useJUnit()
        }
    }

    ios()
    iosSimulatorArm64()

    targets.withType(KotlinNativeTargetWithSimulatorTests::class.java) {
        testRuns["test"].deviceId = "iPhone 14"
    }

    sourceSets {

        val iosSimulatorArm64Main by getting

        val iosMain by getting {
            iosSimulatorArm64Main.dependsOn(this)
        }

        val iosSimulatorArm64Test by getting

        val iosTest by getting {
            iosSimulatorArm64Test.dependsOn(this)
        }
    }
}

tasks.named<TestReport>("allTests").configure {
    configureOutputLocation(testReportsDir("multiplatform"))
}
