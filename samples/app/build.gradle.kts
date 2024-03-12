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
    id("com.android.application")
    id("kotlin-android")
    id("org.jetbrains.compose")
}

repositories {
    maven {
        url = JBComposeDevRepository
    }
}

optIn(DefaultOptIns + "kotlinx.coroutines.ExperimentalCoroutinesApi")

android {
    signingConfigs {
        create("release") {
            storeFile = file(getenvSafe("STORE_FILE") ?: "release.keystore")
            storePassword = getenvSafe("STORE_PASSWORD")
            keyPassword = getenvSafe("KEY_PASSWORD")
            keyAlias = getenvSafe("KEY_ALIAS")
        }
    }
    compileSdk = 34
    namespace = "io.github.xlopec.reader"

    defaultConfig {
        applicationId = "io.github.xlopec.news.reader"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // The following argument makes the Android Test Orchestrator run its
        // "pm clear" command after each test invocation. This command ensures
        // that the app's state is completely cleared between tests.
        testInstrumentationRunnerArguments["clearPackageData"] = "true"
    }

    testOptions {
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
        managedDevices {
            devices {
                maybeCreate<com.android.build.api.dsl.ManagedVirtualDevice>("pixel6api30").apply {
                    device = "Pixel 6"
                    apiLevel = 30
                    systemImageSource = "aosp-atd"
                }
            }
        }
    }

    buildTypes {
        named("release") {
            isShrinkResources = true
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    packaging {
        resources.excludes.add("META-INF/INDEX.LIST")
    }

    lint {
        baseline = file("lint-baseline.xml")
    }

    buildFeatures {
        compose = true
        aidl = false
        renderScript = false
        resValues = false
        shaders = false
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.compose.compiler.get().versionConstraint.requiredVersion
    }

    flavorDimensions += "remote"
    productFlavors {

        create("remote") {
            dimension = "remote"
            applicationIdSuffix = ".remote"
            versionNameSuffix = "(remote debuggable)"
        }

        create("default") {
            dimension = "remote"
        }
    }

    androidComponents {
        beforeVariants(selector().withName("remoteRelease")) { builder ->
            builder.enable = false
        }
    }

    sourceSets {

        maybeCreate("remote")
            .java.srcDirs("remote/kotlin", "main/kotlin")

        maybeCreate("default")
            .java.srcDirs("default/kotlin", "main/kotlin")
    }
}

afterEvaluate {
    tasks.withType<Test>().configureEach {
        configureOutputLocation(testReportsDir(name, "html"), testReportsDir(name, "xml"))
    }
}

dependencies {

    implementation(project(":tea-core"))
    implementation(project(":samples:shared-app-lib"))
    remoteImplementation(project(":samples:shared-app-lib"))

    coreLibraryDesugaring(libs.desugar.jdk)

    implementation(libs.stdlib)

    implementation(libs.coroutines.android)
    implementation(libs.compose.activity)

    implementation(libs.bundles.compose)
    implementation(libs.compose.compiler)
    debugImplementation(libs.compose.tooling)

    implementation(libs.compose.fonts)
    implementation(libs.splashscreen)

    implementation(libs.bundles.accompanist)
    implementation(libs.coil)

    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.logging)
    implementation(libs.logging)

    testImplementation(project(":tea-data"))
    androidTestImplementation(project(":tea-data"))

    androidTestUtil(libs.android.test.orchestrator)
    androidTestImplementation(libs.compose.test.junit)
    debugImplementation(libs.compose.test.manifest)
}
