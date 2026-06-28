/*
 * MIT License
 *
 * Copyright (c) 2026. Maksym Oliinyk.
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

plugins {
    kotlin("jvm")
    `maven-publish`
    id("signing-convention")
    id("documented-convention")
}

version = libraryVersion.toVersionName()
group = "io.github.xlopec"

kotlinJvm {
    explicitApi()
    compilerOptions {
        optIn.addAll(DefaultOptIns)
    }
}

val packSourcesJar = tasks.register<Jar>("packSourcesJar") {
    dependsOn(tasks.classes)
    archiveClassifier.set("sources")
    from(projectSourceSets["main"].allSource)

    group = "release"
    description = "Packs sources jar depending on kotlin plugin applied"
}

val packJavadocJar = tasks.register<Jar>("packJavadocJar") {
    dependsOn(tasks.named("dokkaGeneratePublicationHtml"))
    archiveClassifier.set("javadoc")
    from(documentationDir)

    group = "release"
    description = "Packs javadoc jar"
}

tasks.register<Copy>("copyArtifacts") {
    from(libsDir)
    into(artifactsDir)

    mustRunAfter("publishToSonatype", "publishToMavenLocal")

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

tasks.named("assemble") {
    dependsOn(packSourcesJar, packJavadocJar)
}
