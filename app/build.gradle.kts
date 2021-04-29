/*
 * Copyright (C) 2021. Maksym Oliinyk.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import Libraries.converterGson
import Libraries.coroutinesAndroid
import Libraries.gson
import Libraries.immutableCollections
import Libraries.kotlinStdLib
import Libraries.loggingInterceptor
import Libraries.mongoDb
import Libraries.okHttp
import Libraries.retrofit
import TestLibraries.espressoCore
import TestLibraries.espressoRunner

plugins {
    id("com.android.application")
    id("kotlin-android")
}

android {
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
            isMinifyEnabled = false
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
        useIR = true
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

    implementation(mongoDb)

    implementation(gson)
    // todo remove
    implementation(okHttp)
    implementation(retrofit)
    implementation(converterGson)
    implementation(loggingInterceptor)

    testImplementation(project(":tea-test"))
    testImplementation(coroutinesAndroid)

    androidTestImplementation(espressoRunner)
    androidTestImplementation(espressoCore)
}
