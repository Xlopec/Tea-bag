import gradle.kotlin.dsl.accessors._4912aece6b5b5be71917a8f507c3c7eb.archives
import gradle.kotlin.dsl.accessors._4912aece6b5b5be71917a8f507c3c7eb.publishing
import gradle.kotlin.dsl.accessors._4912aece6b5b5be71917a8f507c3c7eb.signing
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.dokka.gradle.GradleDokkaSourceSetBuilder
import org.jetbrains.kotlin.gradle.plugin.LanguageSettingsBuilder
import java.net.URL

plugins {
    `maven-publish`
    signing
    id("org.jetbrains.dokka")
    kotlin("multiplatform")
    kotlin("native.cocoapods")
}

version = libraryVersion.toVersionName()
group = "io.github.xlopec"

kotlin {
    explicitApi()

    jvm {
        withJava()

        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    ios()

    sourceSets {

        all {
            languageSettings {
                optIn(
                    "kotlinx.coroutines.ExperimentalCoroutinesApi",
                    "kotlin.RequiresOptIn",
                    "kotlinx.coroutines.InternalCoroutinesApi",
                    "kotlinx.coroutines.FlowPreview",
                    "kotlin.ExperimentalStdlibApi",
                    "com.oliynick.max.tea.core.UnstableApi"
                )
            }
        }
    }
}

val sourcesJar by tasks.named("sourcesJar")

val javadocJar by tasks.registering(Jar::class) {
    dependsOn(tasks.named("dokkaHtml"))
    archiveClassifier.set("javadoc")
    from(documentationDir)
}

val copyArtifacts by tasks.registering(Copy::class) {
    group = "release"
    description = "Copies artifacts to the 'artifacts' from project's 'libs' dir for CI"
    from("$buildDir/libs/")
    into("${rootProject.buildDir}/artifacts/${project.name}")
}

tasks.withType<DokkaTask>().configureEach {

    outputDirectory.set(documentationDir)

    dokkaSourceSets {

        configureEach {
            reportUndocumented.set(true)
            includeNonPublic.set(false)
            skipEmptyPackages.set(true)
        }

        named("commonMain") {
            linkSourcesForSourceSet("commonMain")
        }

        named("jvmMain") {
            linkSourcesForSourceSet("jvmMain")
        }

        named("iosMain") {
            linkSourcesForSourceSet("iosMain")
        }
    }
}

publishing {
    publications {

        val projectName = name

        create<MavenPublication>(projectName) {
            artifact(sourcesJar)
            artifact(javadocJar)

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

artifacts {
    archives(sourcesJar)
    archives(javadocJar)
}

fun LanguageSettingsBuilder.optIn(
    vararg annotationNames: String
) = annotationNames.forEach(::optIn)

fun GradleDokkaSourceSetBuilder.linkSourcesForSourceSet(
    name: String
) = sourceLink {
    localDirectory.set(file("src/$name/kotlin"))
    remoteUrl.set(URL("https://github.com/Xlopec/Tea-bag/tree/$branchOrDefault/${project.name}/src/$name/kotlin"))
    remoteLineSuffix.set("#L")
}