import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

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

// Top-level build file where you can add configuration options common to all subprojects/modules.

installGitHooks()

plugins {
    id("common-config")
    alias(libs.plugins.version.check)
    alias(libs.plugins.nexus.publishing)
    alias(libs.plugins.compose) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.sqldelight) apply false
    alias(libs.plugins.kotlinx.serialization) apply false
}

version = libraryVersion.toVersionName()
group = "io.github.xlopec"

// dependencyUpdates fails in parallel mode with Gradle 9+ (https://github.com/ben-manes/gradle-versions-plugin/issues/968)
tasks.named("dependencyUpdates", DependencyUpdatesTask::class.java) {
    doFirst {
        gradle.startParameter.isParallelProjectExecutionEnabled = false
    }

    rejectVersionIf {
        val r = isNonStable(candidate.version) && !isNonStable(currentVersion)

        println("Checking $r ${this.metadata?.id} ${currentVersion} -> ${candidate.version} ")

        r
    }
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
    val regex = "^[0-9,.]+$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}

nexusPublishing {
    repositories {
        sonatype {
            stagingProfileId.set(SonatypeProfileId)
            nexusUrl.set(NexusUrl)
            snapshotRepositoryUrl.set(SnapshotNexusUrl)
            username.set(project.ossrhUser)
            password.set(project.ossrhPassword)
        }
    }

    useStaging.set(!libraryVersion.isSnapshot)
}

allprojects {
    apply {
        plugin("common-config")
    }

    tasks.named("dependencyUpdates", DependencyUpdatesTask::class.java) {
        doFirst {
            gradle.startParameter.isParallelProjectExecutionEnabled = false
        }

        rejectVersionIf {
            val r = isNonStable(candidate.version) && !isNonStable(currentVersion)

         //   println("Checking $r ${this.metadata?.id} ${currentVersion} -> ${candidate.version} ")

            r
        }
    }
}

tasks.register("check") {
    dependsOn(gradle.includedBuild("convention-plugins").task(":check"))
}
