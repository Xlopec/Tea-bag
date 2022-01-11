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

import Libraries.gson
import Libraries.ktorClientCio
import Libraries.ktorClientGson
import Libraries.ktorClientLogging
import Libraries.logback
import TestLibraries.composeJunit
import TestLibraries.composeTestManifest

plugins {
    id("com.android.application")
    id("kotlin-android")
}

tasks.withType<Test>().whenTaskAdded {
    onlyIf { !isCiEnv }
}

android {
    signingConfigs {
        create("release") {
            storeFile = file(getenvSafe("STORE_FILE") ?: "../test.jks")
            storePassword = getenvSafe("STORE_PASSWORD") ?: "qwerty"
            keyPassword = getenvSafe("KEY_PASSWORD") ?: "qwerty"
            keyAlias = getenvSafe("KEY_ALIAS") ?: "test"
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

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.1.0-rc02"
        //kotlinCompilerExtensionVersion = compose
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
            .java.srcDirs("remote/java", "main/java")

        maybeCreate("default")
            .java.srcDirs("default/java", "main/java")
    }
}

dependencies {

    implementation(project(":tea-core"))
    implementation(project(":samples:shared-app-lib"))

    //implementation(project.platform(kotlinStdLibBom))
    implementation(libs.stdlib)
    implementation(libs.stdlib.reflect)

    implementation(libs.coroutines.android)

    implementation(libs.bundles.compose)

    implementation(libs.bundles.accompanist)
    implementation(libs.coil)

    implementation(libs.appcompat)

    implementation(gson)
    implementation(ktorClientCio)
    implementation(ktorClientGson)
    implementation(ktorClientLogging)
    implementation(logback)

    //testImplementation(project(":tea-test"))
    //testImplementation(project(":tea-time-travel"))
    //testImplementation(project(":tea-time-travel-adapter-gson"))

    androidTestImplementation("androidx.test:runner:1.4.0")
    androidTestUtil("androidx.test:orchestrator:1.4.0")
    androidTestImplementation(composeJunit)
    debugImplementation(composeTestManifest)
}
