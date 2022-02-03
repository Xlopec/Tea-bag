plugins {
    kotlin("multiplatform")
}

version = libraryVersion.toVersionName()
group = "io.github.xlopec"

kotlin {
    explicitApi()

    jvm {
        withJava()

        testRuns["test"].executionTask.configure {
            useJUnit()
        }
    }

    ios()
}