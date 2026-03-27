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
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
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

    android {
        compileSdk = 36
        minSdk = 23
        namespace = "io.github.xlopec.shared"
        enableCoreLibraryDesugaring = true

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
            binaryOption("bundleId", "io.github.xlopec.shared")
        }
    }

    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget> {
        compilerOptions {
            freeCompilerArgs.add("-Xexport-kdoc")
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                api(project(":tea-core"))
                api(project(":tea-navigation"))
                implementation(project(":tea-compose"))
                api(libs.arrow.core)
                api(libs.immutable.collections)
                api(libs.coroutines.core)
                api(libs.compose.ui)
                api(libs.compose.runtime)
                api(libs.compose.foundation)
                api(libs.compose.components.ui.tooling.preview)
                api(libs.kotlinx.datetime)
                implementation(libs.compose.components.resources)
                implementation(libs.bundles.coil)
                implementation(libs.compose.material)
                implementation(libs.compose.material.icons.extended)
                implementation(libs.stdlib)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.logging)
                implementation(libs.ktor.client.negotiation)
                implementation(libs.ktor.serialization.json)
                implementation(libs.serialization.core)
                implementation(libs.settings.core)
                implementation(libs.sqldelight.runtime)
                implementation(libs.webview)
                implementation(libs.compose.runtime)
                implementation(libs.ui.tooling.preview)
            }
        }

        commonTest {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        androidMain {
            dependencies {
                implementation(libs.compose.fonts)
                implementation(libs.ktor.client.cio)
                implementation(libs.ktor.client.logging)
                implementation(libs.coroutines.android)
                implementation(libs.compose.activity)
                implementation(libs.sqldelight.driver.android)
            }
        }

        iosMain {
            dependencies {
                implementation(libs.ktor.client.ios)
                implementation(libs.sqldelight.driver.native)
            }
        }
    }
}

tasks.named<TestReport>("allTests").configure {
    configureOutputLocation(testReportsDir("multiplatform"))
}

tasks.withType<Test>().configureEach {
    configureOutputLocation(testReportsDir(name, "html"), testReportsDir(name, "xml"))
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk)
    androidRuntimeClasspath(libs.ui.tooling)
}

sqldelight {
    databases {
        create("AppDatabase") {
            packageName.set("io.github.xlopec.reader.app.storage")
            dialect(libs.sqldelight.sqlight.dialect)
        }
    }
}
