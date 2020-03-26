import BuildPlugins.Versions.intellijVersion
import Libraries.Versions.coroutines
import Libraries.Versions.ktor

const val kotlinVersion = "1.3.71"

object BuildPlugins {

    object Versions {
        const val buildToolsVersion = "3.6.0"
        const val detektVersion = "1.5.1"
        const val dokkaVersion = "0.10.1"
        const val bintrayVersion = "1.8.4"
        const val intellijVersion = "0.4.16"
    }

    const val androidGradlePlugin = "com.android.tools.build:gradle:${Versions.buildToolsVersion}"
    const val kotlinGradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion"
    const val intellijPlugin = "gradle.plugin.org.jetbrains.intellij.plugins:gradle-intellij-plugin:$intellijVersion"
}

object Libraries {
    private object Versions {
        const val coroutines = "1.3.5"
        const val ktor = "1.3.1"
    }

    const val coroutinesCore = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines"
    const val coroutinesAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutines"
    const val coroutinesSwing = "org.jetbrains.kotlinx:kotlinx-coroutines-swing:$coroutines"
    const val coroutinesTest = "org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutines"
    const val kotlinStdLib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion"

    const val ktorServerCore = "io.ktor:ktor-server-core:$ktor"
    const val ktorServerNetty = "io.ktor:ktor-server-netty:$ktor"
    const val ktorServerWebsockets = "io.ktor:ktor-websockets:$ktor"

    const val ktorClientWebsockets = "io.ktor:ktor-client-websockets:$ktor"
    const val ktorClientOkHttp = "io.ktor:ktor-client-okhttp:$ktor"

    const val immutableCollections = "org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.1"
}

object TestLibraries {
    private object Versions {
        const val ktor = "1.2.4"
    }

    const val ktorMockJvm = "io.ktor:ktor-client-mock-jvm:${Versions.ktor}"
    const val ktorServerTests = "io.ktor:ktor-server-tests:${Versions.ktor}"

}

