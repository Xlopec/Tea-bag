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
import Libraries.Versions.ktor

@Deprecated("use version catalogs feature instead")
object Libraries {
    object Versions {
        const val coroutines = "1.6.0"
        const val ktor = "2.0.0-beta-1"
        const val compose = "1.1.0-beta04"
        const val accompanies = "0.21.4-beta"
    }


    const val ktorClientCore = "io.ktor:ktor-client-core:$ktor"
    const val ktorClientIos = "io.ktor:ktor-client-ios:$ktor"
    const val ktorClientWebsockets = "io.ktor:ktor-client-websockets:$ktor"
    const val ktorClientCio = "io.ktor:ktor-client-cio:$ktor"
    const val ktorClientLogging = "io.ktor:ktor-client-logging:$ktor"
    const val ktorClientGson = "io.ktor:ktor-client-gson:$ktor"
    const val ktorClientJson = "io.ktor:ktor-client-json:$ktor"
    const val ktorClientSerialization = "io.ktor:ktor-client-serialization:$ktor"
    const val gson = "com.google.code.gson:gson:2.8.9"

    const val logback = "ch.qos.logback:logback-classic:1.2.3"
    const val atomicfu = "org.jetbrains.kotlinx:atomicfu:0.17.0"
    const val immutableCollections = "org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.4"

    const val appCompat = "androidx.appcompat:appcompat:1.4.0"

    const val sqlDelightAndroidDriver = "com.squareup.sqldelight:android-driver:1.5.3"
    const val sqlDelightNativeDriver = "com.squareup.sqldelight:native-driver:1.5.3"
}

@Deprecated("use version catalogs feature instead")
object TestLibraries {

    const val junit = "junit:junit:4.13.1"
    const val composeJunit = "androidx.compose.ui:ui-test-junit4:$compose"
    const val composeTestManifest = "androidx.compose.ui:ui-test-manifest:$compose"
}
