import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    `maven-publish`
    id("signing-convention")
    id("org.jetbrains.dokka")
}

version = libraryVersion.toVersionName()
group = "io.github.xlopec"

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
    // note that we have preconfigured maven publications
    publications.withType<MavenPublication> {
        artifact(packJavadocJar)
        // other artifacts are added automatically
        pom.configurePom(project.name)
    }

    repositories {
        configureRepositories(project)
    }
}

tasks.withType<DokkaTask>().configureEach {
    outputDirectory.set(documentationDir)
}