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

@file:Suppress("UnstableApiUsage")

rootProject.name = "Tea-bag"

pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}

include(
    ":tea-core",
    ":tea-time-travel",
    ":tea-time-travel-protocol",
    ":tea-time-travel-plugin",
    ":tea-test",
    ":tea-time-travel-adapter-gson",
    ":tea-data",
    ":samples:app",
    ":samples:shared-app-lib"
)

includeBuild("compose-jetbrains-theme") {
    dependencySubstitution {
        substitute(module("com.bybutter.compose:compose-jetbrains-theme")).using(project(":"))
    }
}

dependencyResolutionManagement {
    versionCatalogs {

        create("libs") {
            // 2.0.2 breaks client
            version("ktor", "2.0.0")
            version("coroutines", "1.6.4")
            version("compose", "1.3.0-beta03")
            version("compose-compiler", "1.3.2")
            version("accompanist", "0.26.4-beta")
            version("sqldelight", "1.5.3")
            version("arrow", "1.1.3")

            // Testing

            library("junit", "junit:junit:4.13.2")

            library("kotlin-test", "org.jetbrains.kotlin", "kotlin-test")
                .withoutVersion()

            library("kotlin-test-annotations", "org.jetbrains.kotlin", "kotlin-test-annotations-common")
                .withoutVersion()

            library("compose-test-junit", "androidx.compose.ui", "ui-test-junit4")
                .versionRef("compose")

            library("compose-test-manifest", "androidx.compose.ui", "ui-test-manifest")
                .versionRef("compose")

            library("android-test-orchestrator", "androidx.test:orchestrator:1.4.1")

            // Coroutines

            library("coroutines-core", "org.jetbrains.kotlinx", "kotlinx-coroutines-core")
                .versionRef("coroutines")

            library("coroutines-android", "org.jetbrains.kotlinx", "kotlinx-coroutines-android")
                .versionRef("coroutines")

            library("coroutines-test", "org.jetbrains.kotlinx", "kotlinx-coroutines-test")
                .versionRef("coroutines")

            // Standard library

            library("stdlib", "org.jetbrains.kotlin", "kotlin-stdlib")
                .withoutVersion()

            library("stdlib-reflect", "org.jetbrains.kotlin", "kotlin-reflect")
                .withoutVersion()

            // Ktor server

            library("ktor-server-core", "io.ktor", "ktor-server-core")
                .versionRef("ktor")

            library("ktor-server-netty", "io.ktor", "ktor-server-netty")
                .versionRef("ktor")

            library("ktor-server-websockets", "io.ktor", "ktor-server-websockets")
                .versionRef("ktor")

            library("ktor-server-headers", "io.ktor", "ktor-server-conditional-headers")
                .versionRef("ktor")

            library("ktor-server-logging-jvm", "io.ktor", "ktor-server-call-logging-jvm")
                .versionRef("ktor")

            library("ktor-server-tests", "io.ktor", "ktor-server-tests")
                .versionRef("ktor")

            library("ktor-client-mock-jvm", "io.ktor", "ktor-client-mock-jvm")
                .versionRef("ktor")

            bundle(
                "ktor-server",
                listOf(
                    "ktor-server-core",
                    "ktor-server-netty",
                    "ktor-server-websockets",
                    "ktor-server-headers",
                    "ktor-server-logging-jvm"
                )
            )

            // Ktor client

            library("ktor-client-core", "io.ktor", "ktor-client-core")
                .versionRef("ktor")

            library("ktor-client-ios", "io.ktor", "ktor-client-darwin")
                .versionRef("ktor")

            library("ktor-client-websockets", "io.ktor", "ktor-client-websockets")
                .versionRef("ktor")

            library("ktor-client-cio", "io.ktor", "ktor-client-cio")
                .versionRef("ktor")

            library("ktor-client-logging", "io.ktor", "ktor-client-logging")
                .versionRef("ktor")

            library("ktor-client-negotiation", "io.ktor", "ktor-client-content-negotiation")
                .versionRef("ktor")

            library("ktor-serialization-json", "io.ktor", "ktor-serialization-kotlinx-json")
                .versionRef("ktor")

            // Serialization

            library("serialization-core", "org.jetbrains.kotlinx", "kotlinx-serialization-core").version("1.4.0")

            // Multiplatform settings

            library("settings-core", "com.russhwolf", "multiplatform-settings-no-arg").version("0.9")

            // Compose

            library("compose-ui", "androidx.compose.ui", "ui")
                .versionRef("compose")

            library("compose-foundation", "androidx.compose.foundation", "foundation")
                .versionRef("compose")

            library("compose-foundation-layout", "androidx.compose.foundation", "foundation-layout")
                .versionRef("compose")

            library("compose-material", "androidx.compose.material", "material")
                .versionRef("compose")

            library("compose-icons", "androidx.compose.material", "material-icons-extended")
                .versionRef("compose")

            library("compose-tooling", "androidx.compose.ui", "ui-tooling")
                .versionRef("compose")

            library("compose-tooling-preview", "androidx.compose.ui", "ui-tooling-preview")
                .versionRef("compose")

            library("compose-runtime", "androidx.compose.runtime", "runtime")
                .versionRef("compose")

            library("compose-animation", "androidx.compose.animation", "animation")
                .versionRef("compose")

            library("compose-compiler", "androidx.compose.compiler", "compiler")
                .versionRef("compose-compiler")

            library("compose-activity", "androidx.activity:activity-compose:1.6.0")

            bundle(
                "compose",
                listOf(
                    "compose-ui",
                    "compose-foundation",
                    "compose-foundation-layout",
                    "compose-material",
                    "compose-icons",
                    "compose-runtime",
                    "compose-animation",
                    "compose-activity",
                    "compose-tooling-preview",
                )
            )

            // Downloadable fonts

            library("compose-fonts", "androidx.compose.ui", "ui-text-google-fonts").version("1.3.0-beta03")

            // Splash screen

            library("splashscreen", "androidx.core", "core-splashscreen").version("1.0.0")

            // Accompanist

            library("accompanist-swiperefresh", "com.google.accompanist", "accompanist-swiperefresh")
                .versionRef("accompanist")

            library("accompanist-flow-layout", "com.google.accompanist", "accompanist-flowlayout")
                .versionRef("accompanist")

            bundle(
                "accompanist",
                listOf(
                    "accompanist-swiperefresh",
                    "accompanist-flow-layout"
                )
            )

            // Coil

            library("coil", "io.coil-kt:coil-compose:2.2.2")

            // Collections

            library("collections-immutable", "org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.5")

            // Atomicfu

            library("atomicfu", "org.jetbrains.kotlinx:atomicfu:0.18.3")

            // Logging

            library("logging", "ch.qos.logback:logback-classic:1.4.0")

            // Gson

            library("gson", "com.google.code.gson:gson:2.9.1")

            // Arrow Kt

            library("arrow-core", "io.arrow-kt", "arrow-core").versionRef("arrow")

            // Sqldelight

            library("sqldelight-runtime", "com.squareup.sqldelight", "runtime")
                .versionRef("sqldelight")

            library("sqldelight-driver-android", "com.squareup.sqldelight", "android-driver")
                .versionRef("sqldelight")

            library("sqldelight-driver-native", "com.squareup.sqldelight", "native-driver")
                .versionRef("sqldelight")

            // Desugaring

            library("desugar-jdk", "com.android.tools", "desugar_jdk_libs").version("1.1.5")
        }
    }
}
