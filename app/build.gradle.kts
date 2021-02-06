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

import Libraries.Versions
import Libraries.coroutinesAndroid
import Libraries.immutableCollections
import Libraries.kotlinStdLib

plugins {
    id("com.android.application")
    id("kotlin-android")
}

android {
    compileSdkVersion(30)

    defaultConfig {
        applicationId = "com.oliinyk.max.news.reader"
        minSdkVersion(21)
        targetSdkVersion(30)
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
        kotlinCompilerExtensionVersion = Versions.compose
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
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation(project(":tea-core"))
    implementation(project(":tea-time-travel"))
    implementation(project(":tea-time-travel-adapter-gson"))

    implementation(kotlinStdLib)
    implementation(immutableCollections)
    implementation(coroutinesAndroid)

    implementation("androidx.compose.ui:ui:${Versions.compose}")
    implementation("androidx.compose.foundation:foundation:${Versions.compose}")
    implementation("androidx.compose.foundation:foundation-layout:${Versions.compose}")
    implementation("androidx.compose.material:material:${Versions.compose}")
    implementation("androidx.compose.material:material-icons-core:${Versions.compose}")
    implementation("androidx.compose.material:material-icons-extended:${Versions.compose}")
    implementation("androidx.compose.ui:ui-tooling:${Versions.compose}")
    implementation("androidx.compose.runtime:runtime:${Versions.compose}")
    implementation("androidx.compose.animation:animation:${Versions.compose}")
    implementation("androidx.compose.compiler:compiler:${Versions.compose}")

    implementation("dev.chrisbanes.accompanist:accompanist-insets:${Versions.accompanies}")
    implementation("dev.chrisbanes.accompanist:accompanist-coil:${Versions.accompanies}")

    implementation("androidx.appcompat:appcompat:1.2.0")

    implementation("org.mongodb:stitch-android-sdk:4.1.0")

    implementation("com.google.code.gson:gson:2.8.6")
    implementation("com.squareup.okhttp3:okhttp:4.8.1")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.8.1")

    testImplementation(project(":tea-test"))
    testImplementation(coroutinesAndroid)

    androidTestImplementation("androidx.test:runner:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")

}
