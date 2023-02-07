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
    id("org.jetbrains.intellij")
    id("org.jetbrains.compose")
}

repositories {
    maven {
        url = JBComposeDevRepository
    }
}

val supportedVersions = listOf(
    IDEVersion(Product.IC, 2022, 2),
    IDEVersion(Product.IC, 2022, 3),
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
    channels.set(pluginReleaseChannels)
    dependsOn("runPluginVerifier")
}

tasks.withType<JavaCompile>().configureEach {
    targetCompatibility = "17"
    sourceCompatibility = "17"
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
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

optIn(
    "kotlinx.coroutines.ExperimentalCoroutinesApi",
    "androidx.compose.ui.ExperimentalComposeUiApi",
    "io.github.xlopec.tea.core.ExperimentalTeaApi",
)

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.freeCompilerArgs += listOf("-Xcontext-receivers")

    if (project.findProperty("enableComposeCompilerLogs").toString().toBoolean()) {
        kotlinOptions.freeCompilerArgs += listOf(
            "-P",
            "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=${project.metricsDir.absolutePath}",
            "-P",
            "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=${project.metricsDir.absolutePath}",
        )
    }
}

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

fun shouldUseForcedCoroutinesVersion(
    configuration: Configuration,
    details: DependencyResolveDetails,
    version: String,
): Boolean =
    !configuration.name.startsWith("test") &&
            details.requested.group == "org.jetbrains.kotlinx" &&
            details.requested.module.name.startsWith("kotlinx-coroutines") &&
            details.requested.version != version

configurations.configureEach {
    resolutionStrategy.eachDependency {
        val forcedVersion = "1.6.4"
        if (shouldUseForcedCoroutinesVersion(this@configureEach, this@eachDependency, forcedVersion)) {
            useVersion(forcedVersion)
            because(
                """
                We must use bundled coroutines version, latest compatible coroutines dependency version 
                for IJ 2022.1 is $forcedVersion, see https://www.jetbrains.com/legal/third-party-software/?product=iic&version=2022.3.2
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
    implementation(libs.arrow.core)

    // FIXME this is a temporary workaround
    implementation(compose.desktop.macos_arm64) {
        exclude("org.jetbrains.compose.material")
    }
    implementation(compose.desktop.macos_x64) {
        exclude("org.jetbrains.compose.material")
    }
    implementation(compose.desktop.linux_x64) {
        exclude("org.jetbrains.compose.material")
    }
    implementation(compose.desktop.linux_arm64) {
        exclude("org.jetbrains.compose.material")
    }
    implementation(compose.desktop.windows_x64) {
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
    testImplementation(libs.kotlin.test)
    testImplementation(project(":tea-test"))
    testImplementation(compose("org.jetbrains.compose.ui:ui-test-junit4"))
}
