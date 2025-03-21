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

import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.get
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.URI
import java.nio.file.Paths

const val CommitHashLength = 6
const val SonatypeProfileId = "1c78bd5d6fbb0c"
val NexusUrl = URI("https://s01.oss.sonatype.org/service/local/")
val SnapshotNexusUrl = URI("https://s01.oss.sonatype.org/content/repositories/snapshots/")

private const val RefBranch = "refs/heads/"
private const val RefTag = "refs/tags/"

val isCiEnv: Boolean
    get() = getenvSafe("CI")?.toBoolean() == true

val pluginReleaseChannels: Collection<String>
    get() = when (libraryVersion) {
        is Snapshot -> listOf("dev")
        is Alpha -> listOf("eap")
        is ReleaseCandidate -> listOf("rc")
        is Stable -> listOf()
    }

val branch: String?
    get() = getenvSafe("GITHUB_REF")
        ?.takeIf { it.startsWith(RefBranch) || it.startsWith(RefTag) }
        ?.removePrefix(RefBranch)
        ?.removePrefix(RefTag)
        ?: localBranch

val branchOrDefault: String
    get() = branch ?: "master"

val tag: String?
    get() = getenvSafe("GITHUB_REF")
        ?.takeIf { tag -> tag.startsWith(RefTag) }
        ?.removePrefix(RefTag)

val commitSha: String?
    get() = getenvSafe("GITHUB_SHA")

val libraryVersion: Version
    get() = Version(tag, commitSha)

val Project.ossrhUser: String?
    get() = ciVariable("OSSRH_USER")

val Project.ossrhPassword: String?
    get() = ciVariable("OSSRH_PASSWORD")

val Project.signingKey: String?
    get() = ciVariable("SIGNING_KEY")

val Project.signingPassword: String?
    get() = ciVariable("SIGNING_PASSWORD")

val Project.projectSourceSets: SourceSetContainer
    get() = extensions["sourceSets"] as SourceSetContainer

val Project.documentationDir: Provider<out Directory>
    get() = layout.buildDirectory.map { it.dir("documentation") }

val Project.libsDir: Provider<out Directory>
    get() = layout.buildDirectory.map { it.dir("libs") }

val Project.distributionsDir: Provider<out Directory>
    get() = layout.buildDirectory.map { it.dir("distributions") }

val Project.artifactsDir: Provider<out Directory>
    get() = rootMostProject.layout.buildDirectory.map { it.dir("artifacts").dir(name) }

val Project.rootMostProject: Project
    get() {
        var root = this

        while (root != root.rootProject) {
            root = root.rootProject
        }

        return root
    }

val Project.projectHooksDir: File
    get() = Paths.get(rootDir.path, "hooks").toFile()

val Project.gitHooksDir: File
    get() = Paths.get(rootDir.path, ".git", "hooks").toFile()

val Project.detektConfig: File
    get() = Paths.get(rootDir.path, "detekt", "detekt-config.yml").toFile()

val Project.detektBaseline: File
    get() = Paths.get(rootDir.path, "detekt", "detekt-baseline.xml").toFile()

val Project.metricsDir: Provider<out Directory>
    get() = layout.buildDirectory.map { it.dir("compose_metrics") }

val Project.hasKotlinMultiplatformPlugin: Boolean
    get() = plugins.hasPlugin("org.jetbrains.kotlin.multiplatform")

val Project.hasKotlinJvmPlugin: Boolean
    get() = plugins.hasPlugin("org.jetbrains.kotlin.jvm")

val localBranch: String?
    get() = Runtime.getRuntime().exec("git rev-parse --abbrev-ref HEAD")
        .also { it.waitFor() }
        .takeIf { it.exitValue() == 0 }
        ?.let { BufferedReader(InputStreamReader(it.inputStream)) }
        ?.let { it.use { reader -> reader.readLine().trim() } }

val Project.testReportsDir: Provider<out Directory>
    get() = rootMostProject.layout.buildDirectory.map { it.dir("junit-reports").dir(project.name) }

val Project.htmlTestReportsDir: Provider<out Directory>
    get() = testReportsDir.map { it.dir("html") }

val Project.xmlTestReportsDir: Provider<out Directory>
    get() = testReportsDir.map { it.dir("xml") }

fun Project.testReportsDir(
    vararg subdirs: String,
): Provider<out Directory> = testReportsDir.map { subdirs.fold(it) { acc, path -> acc.dir(path) } }

fun Project.ciVariable(
    name: String,
): String? = getenvSafe(name) ?: getPropertySafe(name)

fun getenvSafe(
    name: String,
): String? =
    System.getenv(name).takeUnless(CharSequence?::isNullOrEmpty)

private fun Project.getPropertySafe(
    name: String,
): String? =
    properties[name]?.toString().takeUnless(CharSequence?::isNullOrEmpty)
