import org.jetbrains.dokka.gradle.DokkaTask
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

plugins {
    `maven-publish`
    `java-library`
    kotlin("jvm")
    // fixme: get rid of bintray
    id("com.jfrog.bintray")
    id("org.jetbrains.dokka")
}

kotlin {
    explicitApi()
}

val sourcesJar by tasks.registering(Jar::class) {
    dependsOn(tasks.classes)
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

val javadocJar by tasks.registering(Jar::class) {
    dependsOn(tasks.named("dokkaJavadoc"))
    archiveClassifier.set("javadoc")
    from("$buildDir/javadoc")
}

val copyArtifacts by tasks.registering(Copy::class) {
    group = "release"
    description = "Copies artifacts to the 'artifacts' dir for CI"
    from("$buildDir/libs/")
    into("${rootProject.buildDir}/artifacts/${project.name}")
}

val releaseLibrary by tasks.creating {
    group = "release"
    description = "Runs build tasks, assembles all the necessary artifacts and publishes them"
    dependsOn("bintrayUpload", copyArtifacts)
}

tasks
    // bintray picks artifacts located from maven local repository, so there is a dependency
    // on publishAllPublicationsToMavenLocalRepository task
    .named("bintrayUpload")
    .dependsOn("publishAllPublicationsToMavenLocalRepository")

copyArtifacts.dependsOn("bintrayUpload")

tasks.withType<DokkaTask>().configureEach {

    outputDirectory.set(buildDir.resolve("javadoc"))

    dokkaSourceSets {
        named("main") {
            reportUndocumented.set(true)
            displayName.set(project.name)
            includeNonPublic.set(false)
            skipEmptyPackages.set(true)

            sourceLink {
                localDirectory.set(file("src/main/java"))
                remoteUrl.set(
                    URL(
                        // fixme make it work for other branches as well
                        "https://github.com/Xlopec/Tea-bag/tree/master/${project.name}/src/main/java"
                    )
                )
            }
            externalDocumentationLink(
                URL("https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/")
            )
            externalDocumentationLink(
                URL("https://javadoc.io/doc/com.google.code.gson/gson/latest/com.google.gson/")
            )
        }
    }
}

publishing {
    publications {
        create<MavenPublication>(name) {
            from(components["java"])
            artifact(sourcesJar)
            artifact(javadocJar)

            groupId = "com.github.Xlopec"
            artifactId = name
            version = versionName
        }
    }

    repositories {
        mavenLocal()
    }
}

artifacts {
    archives(sourcesJar)
    archives(javadocJar)
}

bintray {

    user = "xlopec"
    key = bintrayApiKey
    setPublications(name)

    with(pkg) {

        setLicenses("Apache-2.0")
        repo = "tea-bag"
        name = project.name
        userOrg = "xlopec"
        vcsUrl = "https://github.com/Xlopec/Tea-bag.git"
        websiteUrl = "https://github.com/Xlopec/Tea-bag"
        issueTrackerUrl = "https://github.com/Xlopec/Tea-bag/issues"
        publicDownloadNumbers = true
        githubReleaseNotesFile = "README.md"

        with(version) {

            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZ", Locale.ENGLISH)

            released = format.format(Date())

            val versionName = versionName

            name = versionName
            vcsTag = versionName
        }
    }
}
