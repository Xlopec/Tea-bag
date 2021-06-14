import org.jetbrains.dokka.gradle.DokkaTask
import java.net.URL

plugins {
    `maven-publish`
    signing
    `java-library`
    kotlin("jvm")
    id("org.jetbrains.dokka")
}

kotlin {
    explicitApi()
}

val sourcesJar by tasks.registering(Jar::class) {
    dependsOn(tasks.classes)
    archiveClassifier.set("sources")
    from(projectSourceSets["main"].allSource)
}

val javadocJar by tasks.registering(Jar::class) {
    dependsOn(tasks.named("dokkaJavadoc"))
    archiveClassifier.set("javadoc")
    from("$buildDir/javadoc")
}

val copyArtifacts by tasks.registering(Copy::class) {
    group = "release"
    description = "Copies artifacts to the 'artifacts' from project's 'libs' dir for CI"
    from("$buildDir/libs/")
    into("${rootProject.buildDir}/artifacts/${project.name}")
}

val ciTests by tasks.registering(Test::class) {
    group = "verification"
    description = "Prepares and runs tests relevant for CI build"

    dependsOn(tasks.test.get())
}

val releaseLibrary by tasks.registering {
    group = "release"
    description = "Runs build tasks, assembles all the necessary artifacts and publishes them"
    dependsOn(tasks[if (isCiEnv) "publish" else "publishToMavenLocal"])
    finalizedBy(copyArtifacts)
}

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

        val projectName = name

        create<MavenPublication>(projectName) {
            from(components["java"])
            artifact(sourcesJar)
            artifact(javadocJar)

            groupId = "io.github.xlopec"
            artifactId = projectName
            version = versionName

            pom {
                name.set(projectName)
                description.set("TEA Bag is simple implementation of TEA written in Kotlin. " +
                        "${projectName.capitalize()} is part of this project")
                url.set("https://github.com/Xlopec/Tea-bag.git")

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }

                developers {
                    developer {
                        id.set("Maxxx")
                        name.set("Maksym Oliinyk")
                        url.set("https://github.com/Xlopec")
                        organizationUrl.set("https://github.com/Xlopec")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/Xlopec/Tea-bag.git")
                    developerConnection.set("scm:git:ssh://github.com:Xlopec/Tea-bag.git")
                    url.set("https://github.com/Xlopec/Tea-bag/tree/master")
                }
            }
        }
    }

    repositories {
        mavenLocal()
        maven {
            name = "OSSRH"
            url = ossrhDeploymentUrl(tag)
            credentials {
                username = ossrhUser
                password = ossrhPassword
            }
        }
    }
}

signing {
    useInMemoryPgpKeys(signingKey, signingPassword)

    val publishing: PublishingExtension by project

    sign(publishing.publications)
}

artifacts {
    archives(sourcesJar)
    archives(javadocJar)
}
