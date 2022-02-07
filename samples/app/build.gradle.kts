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

plugins {
    id("com.android.application")
    id("kotlin-android")
}

tasks.withType<Test>().whenTaskAdded {
    onlyIf { !isCiEnv }
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
    compileSdk = 31

    defaultConfig {
        applicationId = "com.oliinyk.max.news.reader"
        minSdk = 21
        targetSdk = 31
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
    }

    buildTypes {
        getByName("release") {
            isShrinkResources = true
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    packagingOptions {
        resources.excludes.add("META-INF/*")
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        @Suppress("UnstableApiUsage")
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

    sourceSets {

        maybeCreate("remote")
            .java.srcDirs("remote/kotlin", "main/kotlin")

        maybeCreate("default")
            .java.srcDirs("default/kotlin", "main/kotlin")
    }
}

dependencies {

    implementation(project(":tea-core"))
    implementation(project(":samples:shared-app-lib"))

    implementation(libs.stdlib)
    implementation(libs.stdlib.reflect)

    implementation(libs.coroutines.android)

    implementation(libs.bundles.compose)
    debugImplementation(libs.compose.tooling)

    implementation(libs.bundles.accompanist)
    implementation(libs.coil)

    implementation(libs.appcompat)

    implementation(libs.gson)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.gson)
    implementation(libs.ktor.client.logging)
    implementation(libs.logging)

    androidTestImplementation(libs.android.test.runner)
    androidTestUtil(libs.android.test.orchestrator)
    androidTestImplementation(libs.compose.test.junit)
    debugImplementation(libs.compose.test.manifest)
}
