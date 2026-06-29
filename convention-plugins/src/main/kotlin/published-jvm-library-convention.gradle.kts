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

import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinJvm
import com.vanniktech.maven.publish.SonatypeHost

plugins {
    kotlin("jvm")
    id("com.vanniktech.maven.publish")
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

exposeCentralPortalProperties()

mavenPublishing {
    configure(
        KotlinJvm(
            javadocJar = JavadocJar.Dokka("dokkaGeneratePublicationHtml"),
            sourcesJar = true,
        ),
    )

    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    coordinates(group.toString(), project.name, version.toString())

    pom { configurePom(project.name) }
}

tasks.register<Copy>("copyArtifacts") {
    from(libsDir)
    into(artifactsDir)

    mustRunAfter(
        "publishAndReleaseToMavenCentral",
        "publishToMavenCentral",
        "publishToMavenLocal",
    )

    group = "release"
    description = "Copies artifacts to the 'artifacts' from project's 'libs' dir for CI"
}
