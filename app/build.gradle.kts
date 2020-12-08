import Libraries.coroutinesAndroid
import Libraries.immutableCollections
import Libraries.kotlinStdLib

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

    composeOptions {
        kotlinCompilerVersion = kotlinVersion
        kotlinCompilerExtensionVersion = "1.0.0-alpha08"
    }

    flavorDimensions += "remoteDebug"
    productFlavors {

        create("remoteDebuggable") {
            dimension = "remoteDebug"
            applicationIdSuffix = ".remoteDebuggable"
            versionNameSuffix = "(remote debuggable)"
        }

        create("default") {
            dimension = "remoteDebug"
        }
    }

    sourceSets {

        maybeCreate("remoteDebuggable")
            .java.srcDirs("remoteDebuggable/java", "main/java")

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

    val composeVersion = "1.0.0-alpha08"

    implementation("androidx.compose.ui:ui:$composeVersion")
    implementation("androidx.compose.foundation:foundation:$composeVersion")
    implementation("androidx.compose.foundation:foundation-layout:$composeVersion")
    implementation("androidx.compose.material:material:$composeVersion")
    implementation("androidx.compose.material:material-icons-core:$composeVersion")
    implementation("androidx.compose.material:material-icons-extended:$composeVersion")
    implementation("androidx.compose.ui:ui-tooling:$composeVersion")
    implementation("androidx.compose.runtime:runtime:$composeVersion")
    implementation("androidx.compose.animation:animation:$composeVersion")
    implementation("androidx.compose.compiler:compiler:$composeVersion")

    val accompaniesVersion = "0.4.0"

    implementation("dev.chrisbanes.accompanist:accompanist-insets:$accompaniesVersion")
    implementation("dev.chrisbanes.accompanist:accompanist-coil:$accompaniesVersion")

    implementation("androidx.appcompat:appcompat:1.2.0")

    implementation("org.mongodb:stitch-android-sdk:4.1.0")

    implementation("com.google.code.gson:gson:2.8.6")
    implementation("com.squareup.okhttp3:okhttp:4.8.1")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.8.1")

    testImplementation(project(path = ":tea-test", configuration = "default"))
    testImplementation(coroutinesAndroid)

    androidTestImplementation("androidx.test:runner:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")

}
