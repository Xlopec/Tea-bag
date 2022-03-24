import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    `maven-publish`
    signing
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
    publications.withType<MavenPublication> {
        artifact(packJavadocJar)

        val projectName = name

        pom {
            name.set(projectName)
            description.set(
                "TEA Bag is simple implementation of TEA written in Kotlin. " +
                        "${projectName.capitalize()} is part of this project"
            )
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

    repositories {
        mavenLocal()
        maven {
            name = "OSSRH"
            url = libraryVersion.toOssrhDeploymentUri()
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

tasks.withType<DokkaTask>().configureEach {
    outputDirectory.set(documentationDir)
}