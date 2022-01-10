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


        }
    }
}