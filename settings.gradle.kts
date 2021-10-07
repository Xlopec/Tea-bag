pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

include(
    ":app",
    ":tea-core",
    ":tea-time-travel",
    ":tea-time-travel-protocol",
    ":tea-time-travel-plugin",
    ":tea-test",
    ":tea-time-travel-adapter-gson",
    ":shared-app-lib"
)
