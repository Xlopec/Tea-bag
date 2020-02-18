/*
 * Copyright (C) 2019 Maksym Oliinyk.
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

buildscript {

    repositories {
        google()
        jcenter()
        maven { setUrl("https://jitpack.io") }
        maven { setUrl("https://plugins.gradle.org/m2/") }
        maven { setUrl("https://kotlin.bintray.com/ktor") }
        maven { setUrl("https://dl.bintray.com/kotlin/kotlin-eap") }
    }
}

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-android-extensions")
}

android {
    compileSdkVersion(29)
    defaultConfig {
        applicationId = "com.max.weatherviewer"
        minSdkVersion(21)
        targetSdkVersion(29)
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

    /*buildFeatures {
        // Enables Jetpack Compose for this module
        compose true
    }*/

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    packagingOptions {
        exclude("META-INF/ktor*")
        exclude("META-INF/kotlin*")
        exclude("META-INF/atomicfu*")
    }
}

androidExtensions {
    isExperimental = true
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation(project(":elm-core-component"))
    implementation(project(":elm-core-component-debug"))

    val kotlin_version = "1.3.60-eap-76"

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlin_version")

    implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3")

    val compose_version = "0.1.0-dev03"


    implementation("androidx.compose:compose-runtime:$compose_version")
    implementation("androidx.ui:ui-framework:$compose_version")
    implementation("androidx.ui:ui-layout:$compose_version")
    implementation("androidx.ui:ui-material:$compose_version")
    implementation("androidx.ui:ui-foundation:$compose_version")
    implementation("androidx.ui:ui-animation:$compose_version")
    implementation("androidx.ui:ui-tooling:$compose_version")

    implementation("com.github.bumptech.glide:glide:4.10.0")

    implementation("androidx.appcompat:appcompat:1.1.0")
    implementation("androidx.activity:activity-ktx:1.0.0")
    implementation("androidx.core:core-ktx:1.1.0")

    implementation("org.mongodb:stitch-android-sdk:4.1.0")

    implementation("com.google.code.gson:gson:2.8.6")
    implementation("com.squareup.okhttp3:okhttp:4.2.0")
    implementation("com.squareup.retrofit2:retrofit:2.6.0")
    implementation("com.squareup.retrofit2:converter-gson:2.6.0")
    implementation("com.squareup.okhttp3:logging-interceptor:3.11.0")

    testImplementation(project(path = ":elm-core-test", configuration = "default"))
    testImplementation("junit:junit:4.12")
    testImplementation(Libraries.coroutinesAndroid)
    testImplementation(Libraries.coroutinesTest)

    androidTestImplementation("androidx.test:runner:1.2.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.2.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.1")

}
