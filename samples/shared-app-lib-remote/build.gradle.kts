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
    id("multiplatform-convention")
    id("com.android.kotlin.multiplatform.library")
}

version = "1.0.0"

kotlin {
    explicitApi()

    compilerOptions {
        optIn.addAll(
            "kotlinx.serialization.ExperimentalSerializationApi",
            "io.github.xlopec.tea.core.ExperimentalTeaApi",
        )
    }

    androidLibrary {
        compileSdk = 36
        minSdk = 23
        namespace = "io.github.xlopec.shared.remote"
        enableCoreLibraryDesugaring = true

        withHostTest {
        }

        androidResources {
            enable = true
        }

        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }

        optimization {
            consumerKeepRules.apply {
                publish = true
                file("proguard-rules.pro")
            }
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()
    applyDefaultHierarchyTemplate()

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "SharedAppLib"
            isStatic = true
        }
    }

    targets.withType(org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTargetWithSimulatorTests::class.java) {
        testRuns["test"].deviceId = "iPhone 17"
    }

    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget> {
        compilerOptions {
            freeCompilerArgs.add("-Xexport-kdoc")
        }
    }

    sourceSets {
        androidMain {
            dependencies {
                api(project(":samples:shared-app-lib"))
                api(project(":tea-time-travel"))
                api(project(":tea-time-travel-adapter-gson"))
                implementation(libs.stdlib)
                implementation(libs.ktor.client.cio)
            }
        }

        getByName("androidHostTest") {
            dependencies {
                implementation(kotlin("test-junit"))
            }
        }
    }
}

tasks.named<TestReport>("allTests").configure {
    configureOutputLocation(testReportsDir("multiplatform"))
}

afterEvaluate {
    tasks.withType<Test>().configureEach {
        configureOutputLocation(testReportsDir(name, "html"), testReportsDir(name, "xml"))
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk)
}