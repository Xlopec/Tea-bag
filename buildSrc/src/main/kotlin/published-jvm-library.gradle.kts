import org.jetbrains.dokka.gradle.DokkaTask
import java.net.URL

plugins {
    kotlin("jvm")
    id("publish-convention")
}

kotlin {
    explicitApi()
}

tasks.withType<DokkaTask>().configureEach {

    dokkaSourceSets {
        named("main") {
            reportUndocumented.set(true)
            includeNonPublic.set(false)
            skipEmptyPackages.set(true)

            linkSourcesForSourceSet(project, "main")
            externalDocumentationLink(
                URL("https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/")
            )
            externalDocumentationLink(
                URL("https://javadoc.io/doc/com.google.code.gson/gson/latest/com.google.gson/")
            )
        }
    }
}
