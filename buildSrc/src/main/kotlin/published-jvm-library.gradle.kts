/*
 * MIT License
 *
 * Copyright (c) 2022. Maksym Oliinyk.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
        mavenLocal()
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
