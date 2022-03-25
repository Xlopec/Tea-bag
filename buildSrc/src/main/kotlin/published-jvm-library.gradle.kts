import gradle.kotlin.dsl.accessors._2cce830b43201e290b93c3ed1e38ead2.publishing
import org.jetbrains.dokka.gradle.DokkaTask
import java.net.URL

plugins {
    kotlin("jvm")
    `maven-publish`
    id("signing-convention")
    id("org.jetbrains.dokka")
}

version = libraryVersion.toVersionName()
group = "io.github.xlopec"

kotlin {
    explicitApi()
}

val packSourcesJar by tasks.creating(Jar::class) {
    dependsOn(tasks.classes)
    archiveClassifier.set("sources")
    from(projectSourceSets["main"].allSource)

    group = "release"
    description = "Packs sources jar depending on kotlin plugin applied"
}

val packJavadocJar by tasks.registering(Jar::class) {
    dependsOn(tasks.named("dokkaHtml"))
    archiveClassifier.set("javadoc")
    from(documentationDir)

    group = "release"
    description = "Packs javadoc jar"
}

val copyArtifacts by tasks.registering(Copy::class) {
    from(libsDir)
    into(artifactsDir)

    group = "release"
    description = "Copies artifacts to the 'artifacts' from project's 'libs' dir for CI"
}

val release by tasks.registering {
    dependsOn(tasks[if (isCiEnv) "publish" else "publishToMavenLocal"])
    finalizedBy(tasks.named("copyArtifacts"))

    group = "release"
    description = "Runs build tasks, assembles all the necessary artifacts and publishes them"
}

publishing {
    publications {
        create<MavenPublication>(project.name) {
            from(components["java"])
            artifact(packJavadocJar)
            artifact(packSourcesJar)
            pom.configurePom(project.name)
        }
    }

    repositories {
        configureRepositories(project)
    }
}

artifacts {
    archives(packSourcesJar)
    archives(packJavadocJar)
}

tasks.withType<DokkaTask>().configureEach {
    outputDirectory.set(documentationDir)

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
