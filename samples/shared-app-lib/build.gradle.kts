import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

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
    id("com.android.library")
    id("com.squareup.sqldelight")
    kotlin("plugin.serialization")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

version = "1.0.0"

@OptIn(ExperimentalKotlinGradlePluginApi::class)
kotlin {
    explicitApi()

    compilerOptions {
        optIn.addAll(
            "kotlinx.serialization.ExperimentalSerializationApi",
            "io.github.xlopec.tea.core.ExperimentalTeaApi",
        )

        optIn.addAll(DefaultOptIns)

        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    androidTarget {
        publishAllLibraryVariants()
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
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
        testRuns["test"].deviceId = "iPhone 15"
    }

    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget> {
        compilerOptions {
            freeCompilerArgs.add("-Xexport-kdoc")
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":tea-core"))
                api(project(":tea-data"))
                api(project(":tea-navigation"))
                api(libs.arrow.core)
                api(libs.collections.immutable)
                api(libs.coroutines.core)
                api(compose.ui)
                api(compose.runtime)
                api(compose.foundation)
                api(compose.components.uiToolingPreview)
                implementation(compose.components.resources)
                implementation(libs.bundles.coil)
                implementation(compose.material)
                implementation(compose.materialIconsExtended)
                implementation(libs.stdlib)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.logging)
                implementation(libs.ktor.client.negotiation)
                implementation(libs.ktor.serialization.json)
                implementation(libs.serialization.core)
                implementation(libs.settings.core)
                implementation(libs.sqldelight.runtime)
                implementation(libs.webview)
                implementation(compose.runtime)
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
                implementation(compose.preview)
                implementation(libs.compose.fonts)
                implementation(libs.ktor.client.cio)
                implementation(libs.ktor.client.logging)
                implementation(libs.coroutines.android)
                implementation(libs.compose.activity)
                implementation(libs.ktor.client.cio)
                implementation(libs.sqldelight.driver.android)
            }
        }

        val androidUnitTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation(libs.junit)
            }
        }

        val iosMain by getting {
            dependencies {
                implementation(libs.ktor.client.ios)
                implementation(libs.sqldelight.driver.native)
            }
        }

        val iosTest by getting
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

android {
    compileSdk = 33
    namespace = "io.github.xlopec.shared"
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 21
        consumerProguardFile("proguard-rules.pro")
    }

    sourceSets {

        maybeCreate("remote")
            .java.srcDirs("remote/kotlin", "main/kotlin")

        maybeCreate("default")
            .java.srcDirs("default/kotlin", "main/kotlin")
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    flavorDimensions += "remote"
    productFlavors {

        create("remote") {
            dimension = "remote"
        }

        create("default") {
            dimension = "remote"
        }
    }

    dependencies {
        remoteApi(project(":tea-time-travel"))
        remoteApi(project(":tea-time-travel-adapter-gson"))
        remoteImplementation(libs.gson)
        debugImplementation(compose.uiTooling)
        coreLibraryDesugaring(libs.desugar.jdk)
    }
}

sqldelight {
    database("AppDatabase") {
        packageName = "io.github.xlopec.reader.app.storage"
    }
}
