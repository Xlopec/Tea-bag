@file:Suppress("UnstableApiUsage")

rootProject.name = "Tea-bag"

enableFeaturePreview("VERSION_CATALOGS")

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
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
        create("test") {
            alias("junit").to("junit:junit:4.13.2")
        }
        create("tea") {
            alias("core").to("io.github.xlopec", "tea-core").withoutVersion()
        }

        create("libs") {

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

            version("coroutines", "1.6.0")

            alias("stdlib")
                .to("org.jetbrains.kotlin", "kotlin-stdlib")
                .withoutVersion()

            alias("stdlib-reflect")
                .to("org.jetbrains.kotlin", "kotlin-reflect")
                .withoutVersion()

            alias("kotlin-test")
                .to("org.jetbrains.kotlin", "kotlin-test")
                .withoutVersion()

            version("ktor", "2.0.0-beta-1")

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

            bundle("ktor-server",
                listOf(
                    "ktor-server-core",
                    "ktor-server-netty",
                    "ktor-server-websockets",
                    "ktor-server-headers",
                    "ktor-server-logging-jvm"
                )
            )
        }
    }
}