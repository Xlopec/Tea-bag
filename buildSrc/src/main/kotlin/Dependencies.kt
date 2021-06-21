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

const val kotlinVersion = "1.5.10"

object Libraries {
    object Versions {
        const val coroutines = "1.5.0"
        const val ktor = "1.6.0"
        const val compose = "1.0.0-beta08"
        const val accompanies = "0.11.0"
    }

    const val coroutinesCore = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines"
    const val coroutinesAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutines"
    const val coroutinesSwing = "org.jetbrains.kotlinx:kotlinx-coroutines-swing:$coroutines"
    const val coroutinesTest = "org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutines"
    const val kotlinStdLib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion"
    const val kotlinStdLibReflect = "org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion"

    const val ktorServerCore = "io.ktor:ktor-server-core:$ktor"
    const val ktorServerNetty = "io.ktor:ktor-server-netty:$ktor"
    const val ktorServerWebsockets = "io.ktor:ktor-websockets:$ktor"

    const val ktorClientWebsockets = "io.ktor:ktor-client-websockets:$ktor"
    const val ktorClientCio = "io.ktor:ktor-client-cio:$ktor"
    const val ktorClientLogging = "io.ktor:ktor-client-logging:$ktor"
    const val ktorClientGson = "io.ktor:ktor-client-gson:$ktor"
    const val ktorClientJson = "io.ktor:ktor-client-json:$ktor"
    const val ktorClientSerialization = "io.ktor:ktor-client-serialization:$ktor"
    const val gson = "com.google.code.gson:gson:2.8.7"

    const val logback = "ch.qos.logback:logback-classic:1.2.3"
    const val atomicfu = "org.jetbrains.kotlinx:atomicfu:0.15.1"
    const val immutableCollections = "org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.3"

    const val composeUi = "androidx.compose.ui:ui:$compose"
    const val composeFoundation = "androidx.compose.foundation:foundation:$compose"
    const val composeFoundationLayout = "androidx.compose.foundation:foundation-layout:$compose"
    const val composeMaterial = "androidx.compose.material:material:$compose"
    const val composeMaterialIconsExtended =
        "androidx.compose.material:material-icons-extended:$compose"
    const val composeUiTooling = "androidx.compose.ui:ui-tooling:$compose"
    const val composeRuntime = "androidx.compose.runtime:runtime:$compose"
    const val composeAnimation = "androidx.compose.animation:animation:$compose"
    const val composeCompiler = "androidx.compose.compiler:compiler:$compose"
    const val composeActivity = "androidx.activity:activity-compose:1.3.0-alpha08"

    const val accompaniestInsets = "com.google.accompanist:accompanist-insets:$accompanies"
    const val accompaniestCoil = "com.google.accompanist:accompanist-coil:$accompanies"
    const val accompaniestSwipeRefresh =
        "com.google.accompanist:accompanist-swiperefresh:$accompanies"

    const val appCompat = "androidx.appcompat:appcompat:1.2.0"
}

object TestLibraries {
    private object Versions {
        const val ktor = "1.6.0"
    }

    const val junit = "junit:junit:4.13.1"
    const val junitRunner = "io.kotlintest:kotlintest-runner-junit4:3.4.2"
    const val espressoRunner = "androidx.test:runner:1.3.0"
    const val espressoCore = "androidx.test.espresso:espresso-core:3.3.0"
    const val ktorMockJvm = "io.ktor:ktor-client-mock-jvm:${Versions.ktor}"
    const val ktorServerTests = "io.ktor:ktor-server-tests:${Versions.ktor}"
}
