import org.jetbrains.compose.compose

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
    kotlin("native.cocoapods")
    id("com.android.library")
    id("com.squareup.sqldelight")
    kotlin("plugin.serialization")
    id("org.jetbrains.compose")
}

version = "1.0.0"

repositories {
    maven {
        url = JBComposeDevRepository
    }
}

kotlin {

    optIn("kotlinx.serialization.ExperimentalSerializationApi", "io.github.xlopec.tea.core.ExperimentalTeaApi")

    android {
        publishAllLibraryVariants()
    }

    ios()

    cocoapods {
        summary = "Shared app lib with common code"
        homepage = "Link to the Shared Module homepage"
        ios.deploymentTarget = "14.1"
        framework {
            baseName = "SharedAppLib"
        }
        podfile = project.file("../iosApp/Podfile")
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":tea-core"))
                api(project(":tea-data"))
                api(libs.collections.immutable)
                api(libs.coroutines.core)
                implementation(libs.stdlib)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.logging)
                implementation(libs.ktor.client.negotiation)
                implementation(libs.ktor.serialization.json)
                implementation(libs.serialization.core)
                implementation(libs.settings.core)
                implementation(libs.sqldelight.runtime)
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
                implementation(libs.ktor.client.cio)
                implementation(libs.sqldelight.driver.android)
            }
        }

        val androidTest by getting {
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
    val reportDir = testReportsDir("multiplatform")

    description = "$description Also copies test reports to $reportDir"
    destinationDir = reportDir
}

afterEvaluate {
    tasks.withType<Test>().configureEach {
        description = "$description Also copies test reports to ${testReportsDir(name)}"

        reports {
            html.outputLocation.set(testReportsDir(name, "html"))
            junitXml.outputLocation.set(testReportsDir(name, "xml"))
        }
    }
}

android {
    compileSdk = 31
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 21
        targetSdk = 31
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
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
        coreLibraryDesugaring(libs.desugar.jdk)
    }
}

sqldelight {
    database("AppDatabase") {
        packageName = "io.github.xlopec.reader.app.storage"
    }
}
