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

import Libraries.Versions.accompanies
import Libraries.Versions.compose
import Libraries.Versions.coroutines
import Libraries.Versions.ktor

@Deprecated("use version catalogs feature instead")
object Libraries {
    object Versions {
        const val coroutines = "1.6.0"
        const val ktor = "2.0.0-beta-1"
        const val compose = "1.1.0-beta04"
        const val accompanies = "0.21.4-beta"
    }

    const val coroutinesBom = "org.jetbrains.kotlinx:kotlinx-coroutines-bom:$coroutines"
    const val coroutinesCore = "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0"
    const val coroutinesAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.0"
    const val coroutinesSwing = "org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.6.0"
    const val coroutinesTest = "org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.0"
    const val kotlinStdLibBom = "org.jetbrains.kotlin:kotlin-bom"
    const val kotlinStdLib = "org.jetbrains.kotlin:kotlin-stdlib"
    const val kotlinStdLibReflect = "org.jetbrains.kotlin:kotlin-reflect"

    const val ktorServerCore = "io.ktor:ktor-server-core:$ktor"
    const val ktorServerNetty = "io.ktor:ktor-server-netty:$ktor"
    const val ktorServerWebsockets = "io.ktor:ktor-server-websockets:$ktor"
    const val ktorServerConditionalHeaders = "io.ktor:ktor-server-conditional-headers:$ktor"
    const val ktorServerCallLoggingJvm = "io.ktor:ktor-server-call-logging-jvm:$ktor"

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

    const val composeUi = "androidx.compose.ui:ui:$compose"
    const val composeFoundation = "androidx.compose.foundation:foundation:$compose"
    const val composeFoundationLayout = "androidx.compose.foundation:foundation-layout:$compose"
    const val composeMaterial = "androidx.compose.material:material:$compose"
    const val composeMaterialIconsExtended =
        "androidx.compose.material:material-icons-extended:$compose"
    const val composeUiTooling = "androidx.compose.ui:ui-tooling:$compose"
    const val composeRuntime = "androidx.compose.runtime:runtime:$compose"
    const val composeAnimation = "androidx.compose.animation:animation:$compose"
    const val composeCompiler = "androidx.compose.compiler:compiler:1.1.0-rc02"// compatible with kotlin 1.6.10
    const val composeActivity = "androidx.activity:activity-compose:1.4.0"

    const val accompaniestInsets = "com.google.accompanist:accompanist-insets:$accompanies"
    const val accompaniestCoil = "io.coil-kt:coil-compose:1.4.0"
    const val accompaniestSwipeRefresh =
        "com.google.accompanist:accompanist-swiperefresh:$accompanies"

    const val appCompat = "androidx.appcompat:appcompat:1.4.0"

    const val sqlDelightAndroidDriver = "com.squareup.sqldelight:android-driver:1.5.3"
    const val sqlDelightNativeDriver = "com.squareup.sqldelight:native-driver:1.5.3"
}

@Deprecated("use version catalogs feature instead")
object TestLibraries {
    private object Versions {
        const val ktor = "2.0.0-beta-1"
    }

    const val junit = "junit:junit:4.13.1"
    const val composeJunit = "androidx.compose.ui:ui-test-junit4:$compose"
    const val composeTestManifest = "androidx.compose.ui:ui-test-manifest:$compose"
    const val ktorMockJvm = "io.ktor:ktor-client-mock-jvm:${Versions.ktor}"
    const val ktorServerTests = "io.ktor:ktor-server-tests:${Versions.ktor}"
    const val kotlinTest = "org.jetbrains.kotlin:kotlin-test"
}
