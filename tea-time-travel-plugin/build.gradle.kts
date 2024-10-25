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

import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType

plugins {
    `maven-publish`
    `java-library`
    kotlin("jvm")
    id("org.jetbrains.intellij.platform")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

val supportedVersions = listOf(
    IDEVersion(Product.IC, 2024, 2),
    IDEVersion(Product.IC, 2024, 1),
)

/*intellij {
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
}*/

/*tasks.named<org.jetbrains.intellij.tasks.RunPluginVerifierTask>("runPluginVerifier") {
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
}*/

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

kotlin {
    compilerOptions {
        optIn.addAll(DefaultOptIns)

        freeCompilerArgs.addAll("-Xcontext-receivers")

        if (project.findProperty("enableComposeCompilerLogs").toString().toBoolean()) {
            freeCompilerArgs.addAll(
                "-P",
                "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=${project.metricsDir.get().asFile.absolutePath}",
                "-P",
                "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=${project.metricsDir.get().asFile.absolutePath}",
            )
        }
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

intellijPlatform {
    pluginConfiguration {
        id = "com.github.Xlopec.elm.time.travel"
        name = "Time Travel Debugger"
        vendor {
            name = "Maksym Oliinyk"
            email = "maksimolejn720@gmail.com"
        }
        version.set(libraryVersion.toVersionName())
        ideaVersion {
            sinceBuild.set(supportedVersions.oldest().buildNumber)
        }
    }

    publishing {
        token.set(ciVariable("PUBLISH_PLUGIN_TOKEN"))
        channels.set(pluginReleaseChannels)
    }

    pluginVerification {
        ides {
            ide(type = IntelliJPlatformType.IntellijIdeaCommunity, supportedVersions.latest().versionName)
            ide(type = IntelliJPlatformType.IntellijIdeaCommunity, supportedVersions.oldest().versionName)
        }
    }
}

dependencies {
    intellijPlatform {
        pluginVerifier()
        val idePath = getenvSafe("IJ_C")

        if (isCiEnv || idePath == null) {
            val ideVersion = supportedVersions.latest().versionName
            logger.info("IDE of version $ideVersion will be used")
            intellijIdeaCommunity(ideVersion)
        } else {
            logger.info("Local IDE distribution will be used located at $idePath")
            local(idePath)
        }

        instrumentationTools()
        bundledPlugin("com.intellij.java")
    }

    implementation(project(":tea-core")) {
        exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-core")
    }
    implementation(project(":tea-time-travel-protocol"))
    implementation(project(":tea-time-travel-adapter-gson"))
    implementation(project(":tea-data"))
    implementation(libs.kotlinx.datetime)

    implementation(libs.stdlib)
    implementation(libs.stdlib.reflect)
    implementation(libs.arrow.core)

    implementation(libs.jewel.ide.bridge)

    // FIXME this is a temporary workaround
    implementation(compose.desktop.macos_arm64) {
        exclude(group = "org.jetbrains.compose.material")
        exclude(group = "org.jetbrains.kotlinx")
    }
    implementation(compose.desktop.macos_x64) {
        exclude(group = "org.jetbrains.compose.material")
        exclude(group = "org.jetbrains.kotlinx")
    }
    implementation(compose.desktop.linux_x64) {
        exclude(group = "org.jetbrains.compose.material")
        exclude(group = "org.jetbrains.kotlinx")
    }
    implementation(compose.desktop.linux_arm64) {
        exclude(group = "org.jetbrains.compose.material")
        exclude(group = "org.jetbrains.kotlinx")
    }
    implementation(compose.desktop.windows_x64) {
        exclude(group = "org.jetbrains.compose.material")
        exclude(group = "org.jetbrains.kotlinx")
    }

    @OptIn(ExperimentalComposeLibrary::class)
    implementation(compose.desktop.components.splitPane) {
        exclude(group = "org.jetbrains.kotlinx")
    }

    implementation(libs.logging)

    implementation(libs.bundles.ktor.server) {
        exclude(group = "org.jetbrains.kotlinx")
    }

    implementation(libs.collections.immutable)

    testImplementation(libs.jewel.ui.standalone)
    testImplementation(compose.desktop.uiTestJUnit4)
    testImplementation(libs.ktor.server.tests)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.kotlin.test)
    testImplementation(project(":tea-test"))
}
