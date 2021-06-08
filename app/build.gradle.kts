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

import Libraries.Versions.compose
import Libraries.accompaniestCoil
import Libraries.accompaniestInsets
import Libraries.accompaniestSwipeRefresh
import Libraries.appCompat
import Libraries.composeActivity
import Libraries.composeAnimation
import Libraries.composeCompiler
import Libraries.composeFoundation
import Libraries.composeFoundationLayout
import Libraries.composeMaterial
import Libraries.composeMaterialIconsExtended
import Libraries.composeRuntime
import Libraries.composeUi
import Libraries.composeUiTooling
import Libraries.coroutinesAndroid
import Libraries.gson
import Libraries.immutableCollections
import Libraries.kotlinStdLib
import Libraries.kotlinStdLibReflect
import Libraries.ktorClientCio
import Libraries.ktorClientGson
import Libraries.ktorClientLogging
import Libraries.logback
import TestLibraries.espressoCore
import TestLibraries.espressoRunner

plugins {
    id("com.android.application")
    id("kotlin-android")
}

android {
    signingConfigs {
        create("release") {
            storeFile = file(getenvSafe("STORE_FILE") ?: "release.keystore")
            storePassword = getenvSafe("STORE_PASSWORD")
            keyPassword = getenvSafe("KEY_PASSWORD")
            keyAlias = getenvSafe("KEY_ALIAS")
        }
    }
    compileSdk = 30

    defaultConfig {
        applicationId = "com.oliinyk.max.news.reader"
        minSdk = 21
        targetSdk = 30
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        kotlinCompilerExtensionVersion = compose
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

    remoteImplementation(project(":tea-time-travel"))
    remoteImplementation(project(":tea-time-travel-adapter-gson"))

    implementation(kotlinStdLib)
    implementation(kotlinStdLibReflect)

    implementation(immutableCollections)
    implementation(coroutinesAndroid)

    implementation(composeUi)
    implementation(composeFoundation)
    implementation(composeFoundationLayout)
    implementation(composeMaterial)
    implementation(composeMaterialIconsExtended)
    implementation(composeUiTooling)
    implementation(composeRuntime)
    implementation(composeAnimation)
    implementation(composeCompiler)
    implementation(composeActivity)

    implementation(accompaniestInsets)
    implementation(accompaniestCoil)
    implementation(accompaniestSwipeRefresh)

    implementation(appCompat)

    implementation(gson)
    implementation(ktorClientCio)
    implementation(ktorClientGson)
    implementation(ktorClientLogging)
    implementation(logback)

    testImplementation(project(":tea-test"))
    testImplementation(coroutinesAndroid)
    testImplementation(project(":tea-time-travel"))
    testImplementation(project(":tea-time-travel-adapter-gson"))

    androidTestImplementation(espressoRunner)
    androidTestImplementation(espressoCore)
}
