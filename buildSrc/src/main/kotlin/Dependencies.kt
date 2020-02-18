import Libraries.Versions.coroutines
import Libraries.Versions.ktor

const val kotlinVersion = "1.3.61"

object BuildPlugins {

    object Versions {
        const val buildToolsVersion = "3.5.3"
        const val detektVersion = "1.5.1"
        const val dokkaVersion = "0.10.1"
    }

    const val androidGradlePlugin = "com.android.tools.build:gradle:${Versions.buildToolsVersion}"
    const val kotlinGradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion"
    const val androidMaven = "com.github.dcendents:android-maven-gradle-plugin:1.4.1"
}

object AndroidSdk {
    const val min = 15
    const val compile = 28
    const val target = compile
}

object Libraries {
    private object Versions {
        const val coroutines = "1.3.3"
        const val ktor = "1.2.4"
    }

    const val coroutinesCore = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines"
    const val coroutinesAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutines"
    const val coroutinesSwing = "org.jetbrains.kotlinx:kotlinx-coroutines-swing:$coroutines"
    const val coroutinesTest = "org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutines"
    const val kotlinStdLib     = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion"
    const val kotlinReflect     = "org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion"

    const val ktorServerCore = "io.ktor:ktor-server-core:$ktor"
    const val ktorServerNetty = "io.ktor:ktor-server-netty:$ktor"
    const val ktorWebsockets = "io.ktor:ktor-client-websockets:$ktor"
    const val ktorServerWebsockets = "io.ktor:ktor-websockets:$ktor"
    const val ktorOkHttp = "io.ktor:ktor-client-okhttp:$ktor"
}

object TestLibraries {
    private object Versions {
        const val junit4 = "4.12"
        const val testRunner = "1.1.0-alpha4"
        const val espresso = "3.1.0-alpha4"
        const val ktor = "1.2.4"
    }
    const val junit4     = "junit:junit:${Versions.junit4}"
    const val ktorMockJvm = "io.ktor:ktor-client-mock-jvm:${Versions.ktor}"
    const val ktorServerTests = "io.ktor:ktor-server-tests:${Versions.ktor}"
    const val coroutinesTest = "org.jetbrains.kotlinx:kotlinx-coroutines-test:1.3.3"

}

