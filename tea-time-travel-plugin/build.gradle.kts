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

import org.jetbrains.compose.compose
import org.jetbrains.intellij.tasks.PatchPluginXmlTask
import org.jetbrains.intellij.tasks.PublishPluginTask

plugins {
    `maven-publish`
    `java-library`
    kotlin("jvm")
    intellij()
    id("org.jetbrains.compose")
}

repositories {
    maven {
        setUrl("https://repo1.maven.org/maven2/")
    }
}

val supportedVersions = listOf(
    IDEVersion(Product.IC, 2021, 1),
    IDEVersion(Product.IC, 2021, 2),
    IDEVersion(Product.IC, 2021, 3),
    IDEVersion(Product.IC, 2022, 1),
)

intellij {
    val idePath = getenvSafe("IJ_C")

    if (isCiEnv || idePath == null) {
        val ideVersion = supportedVersions.latest().versionName
        logger.info("IDE of version $ideVersion will be used")
        version.set(ideVersion)
    } else {
        logger.info("Local IDE distribution will be used located at $idePath")
        localPath.set(idePath)
    }

    plugins.add("com.intellij.java")
}

tasks.named<org.jetbrains.intellij.tasks.RunPluginVerifierTask>("runPluginVerifier") {
    ideVersions.set(supportedVersions.map { it.versionName })
}

tasks.named<PatchPluginXmlTask>("patchPluginXml") {
    version.set(libraryVersion.toVersionName())
    sinceBuild.set(supportedVersions.oldest().buildNumber)
}

tasks.named<PublishPluginTask>("publishPlugin") {
    token.set(ciVariable("PUBLISH_PLUGIN_TOKEN"))
    channels.set(PluginReleaseChannels)
    dependsOn("runPluginVerifier")
}

val copyArtifacts by tasks.registering(Copy::class) {
    from(libsDir, distributionsDir)
    into(artifactsDir)

    mustRunAfter("publishPlugin")

    group = "release"
    description = "Copies artifacts to the 'artifacts' from project's 'libs' dir for CI"
}

val allTests by tasks.creating(Task::class) {
    dependsOn("test")
}

tasks.withType<Test>().configureEach {
    reports {
        html.outputLocation.set(htmlTestReportsDir)
        junitXml.outputLocation.set(xmlTestReportsDir)
    }
}

optIn(
    "kotlinx.coroutines.ExperimentalCoroutinesApi",
    "io.github.xlopec.tea.core.ExperimentalTeaApi",
)

sourceSets {

    main {
        resources {
            srcDir("resources")
        }
    }

    test {
        java {
            setSrcDirs(listOf("src/test/integration/kotlin", "src/test/unit/kotlin"))
        }
    }
}

configurations.configureEach {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.jetbrains.kotlinx" && requested.module.name.startsWith("kotlinx-coroutines")) {
            val forcedVersion = "1.5.2"
            useVersion(forcedVersion)
            // https://www.jetbrains.com/legal/third-party-software/?product=iic&version=2022.1
            because(
                """
                We must use bundled coroutines version, latest compatible coroutines dependency version 
                for IJ 2022.1 is $forcedVersion, see https://www.jetbrains.com/legal/third-party-software/?product=iic&version=2022.1 
            """.trimIndent()
            )
        }
    }
}

dependencies {

    implementation(project(":tea-core"))
    implementation(project(":tea-time-travel-protocol"))
    implementation(project(":tea-time-travel-adapter-gson"))
    implementation(project(":tea-data"))

    implementation(libs.stdlib)
    implementation(libs.stdlib.reflect)

    implementation(compose.desktop.currentOs) {
        exclude("org.jetbrains.compose.material")
    }
    @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
    implementation(compose.desktop.components.splitPane)
    implementation("com.bybutter.compose:compose-jetbrains-theme")
    implementation(libs.logging)

    implementation(libs.bundles.ktor.server)
    implementation(libs.coroutines.core)
    implementation(libs.collections.immutable)

    testImplementation(libs.ktor.server.tests)
    testImplementation(libs.coroutines.test)
    testImplementation(project(":tea-test"))
    testImplementation("io.kotlintest:kotlintest-assertions:3.4.2")
    testImplementation(compose("org.jetbrains.compose.ui:ui-test-junit4"))
}
