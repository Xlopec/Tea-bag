import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    id("multiplatform-library")
    id("publish-convention")
}

tasks.withType<DokkaTask>().configureEach {

    dokkaSourceSets {

        configureEach {
            reportUndocumented.set(true)
            includeNonPublic.set(false)
            skipEmptyPackages.set(true)
        }

        listOf("commonMain", "jvmMain", "iosMain").forEach { sourceSet ->
            named(sourceSet) {
                linkSourcesForSourceSet(project, sourceSet)
            }
        }
    }
}