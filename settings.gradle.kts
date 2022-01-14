@file:Suppress("UnstableApiUsage")

rootProject.name = "Tea-bag"

enableFeaturePreview("VERSION_CATALOGS")

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
    ":shared-entities",
    ":samples:app",
    ":samples:shared-app-lib"
)

dependencyResolutionManagement {
    versionCatalogs {

        create("tea") {
            alias("core").to("io.github.xlopec", "tea-core").withoutVersion()
        }

        create("libs") {

            version("ktor", "2.0.0-beta-1")
            version("coroutines", "1.6.0")
            version("compose", "1.2.0-alpha01")
            version("accompanist", "0.21.4-beta")
            version("sqldelight", "1.5.3")

            // Testing

            alias("junit").to("junit:junit:4.13.2")

            alias("kotlin-test")
                .to("org.jetbrains.kotlin", "kotlin-test")
                .withoutVersion()

            alias("compose-test-junit")
                .to("androidx.compose.ui", "ui-test-junit4")
                .versionRef("compose")

            alias("compose-test-manifest")
                .to("androidx.compose.ui", "ui-test-manifest")
                .versionRef("compose")

            alias("android-test-orchestrator")
                .to("androidx.test:orchestrator:1.4.1")

            alias("android-test-runner").to("androidx.test:orchestrator:1.4.0")

            // Coroutines

            alias("coroutines-core")
                .to("org.jetbrains.kotlinx", "kotlinx-coroutines-core")
                .versionRef("coroutines")

            alias("coroutines-android")
                .to("org.jetbrains.kotlinx", "kotlinx-coroutines-android")
                .versionRef("coroutines")

            alias("coroutines-swing")
                .to("org.jetbrains.kotlinx", "kotlinx-coroutines-swing")
                .versionRef("coroutines")

            alias("coroutines-test")
                .to("org.jetbrains.kotlinx", "kotlinx-coroutines-test")
                .versionRef("coroutines")

            // Standard library

            alias("stdlib")
                .to("org.jetbrains.kotlin", "kotlin-stdlib")
                .withoutVersion()

            alias("stdlib-reflect")
                .to("org.jetbrains.kotlin", "kotlin-reflect")
                .withoutVersion()

            // Ktor server

            alias("ktor-server-core")
                .to("io.ktor", "ktor-server-core")
                .versionRef("ktor")

            alias("ktor-server-netty")
                .to("io.ktor", "ktor-server-netty")
                .versionRef("ktor")

            alias("ktor-server-websockets")
                .to("io.ktor", "ktor-server-websockets")
                .versionRef("ktor")

            alias("ktor-server-headers")
                .to("io.ktor", "ktor-server-conditional-headers")
                .versionRef("ktor")

            alias("ktor-server-logging-jvm")
                .to("io.ktor", "ktor-server-call-logging-jvm")
                .versionRef("ktor")

            alias("ktor-server-tests")
                .to("io.ktor", "ktor-server-tests")
                .versionRef("ktor")

            alias("ktor-client-mock-jvm")
                .to("io.ktor", "ktor-client-mock-jvm")
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

            alias("ktor-client-core")
                .to("io.ktor", "ktor-client-core")
                .versionRef("ktor")

            alias("ktor-client-ios")
                .to("io.ktor", "ktor-client-ios")
                .versionRef("ktor")

            alias("ktor-client-websockets")
                .to("io.ktor", "ktor-client-websockets")
                .versionRef("ktor")

            alias("ktor-client-cio")
                .to("io.ktor", "ktor-client-cio")
                .versionRef("ktor")

            alias("ktor-client-logging")
                .to("io.ktor", "ktor-client-logging")
                .versionRef("ktor")

            alias("ktor-client-gson")
                .to("io.ktor", "ktor-client-gson")
                .versionRef("ktor")

            alias("ktor-client-json")
                .to("io.ktor", "ktor-client-json")
                .versionRef("ktor")

            alias("ktor-client-serialization")
                .to("io.ktor", "ktor-client-serialization")
                .versionRef("ktor")

            // Compose

            alias("compose-ui")
                .to("androidx.compose.ui", "ui")
                .versionRef("compose")

            alias("compose-foundation")
                .to("androidx.compose.foundation", "foundation")
                .versionRef("compose")

            alias("compose-foundation-layout")
                .to("androidx.compose.foundation", "foundation-layout")
                .versionRef("compose")

            alias("compose-material")
                .to("androidx.compose.material", "material")
                .versionRef("compose")

            alias("compose-icons")
                .to("androidx.compose.material", "material-icons-extended")
                .versionRef("compose")

            alias("compose-tooling")
                .to("androidx.compose.ui", "ui-tooling")
                .versionRef("compose")

            alias("compose-runtime")
                .to("androidx.compose.runtime", "runtime")
                .versionRef("compose")

            alias("compose-animation")
                .to("androidx.compose.animation", "animation")
                .versionRef("compose")

            alias("compose-compiler")
                .to("androidx.compose.compiler", "compiler")
                .versionRef("compose")

            alias("compose-activity").to("androidx.activity:activity-compose:1.4.0")

            bundle(
                "compose",
                listOf(
                    "compose-ui",
                    "compose-foundation",
                    "compose-foundation-layout",
                    "compose-material",
                    "compose-icons",
                    "compose-tooling",
                    "compose-runtime",
                    "compose-animation",
                    "compose-compiler",
                    "compose-activity",
                )
            )

            // Accompanist

            alias("accompanist-insets")
                .to("com.google.accompanist", "accompanist-insets")
                .versionRef("accompanist")

            alias("accompanist-swiperefresh")
                .to("com.google.accompanist", "accompanist-swiperefresh")
                .versionRef("accompanist")

            bundle("accompanist", listOf("accompanist-insets", "accompanist-swiperefresh"))

            // Coil

            alias("coil").to("io.coil-kt:coil-compose:1.4.0")

            // App compat

            alias("appcompat").to("androidx.appcompat:appcompat:1.4.0")

            // Collections

            alias("collections-immutable").to("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.4")

            // Atomicfu

            alias("atomicfu").to("org.jetbrains.kotlinx:atomicfu:0.17.0")

            // Logging

            alias("logging").to("ch.qos.logback:logback-classic:1.2.3")

            // Gson

            alias("gson").to("com.google.code.gson:gson:2.8.9")

            // Sqldelight

            alias("sqldelight-driver-android")
                .to("com.squareup.sqldelight", "android-driver")
                .versionRef("sqldelight")

            alias("sqldelight-driver-native")
                .to("com.squareup.sqldelight", "native-driver")
                .versionRef("sqldelight")
        }
    }
}