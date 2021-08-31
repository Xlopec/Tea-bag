/*
 * MIT License
 *
 * Copyright (c) 2021. Maksym Oliinyk.
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
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.get
import java.io.File
import java.net.URI
import java.nio.file.Paths

const val CommitHashLength = 6

val isCiEnv: Boolean
    get() = getenvSafe("CI")?.toBoolean() == true

val pluginReleaseChannels: Array<String>
    get() = when (libraryVersion) {
        is Snapshot -> arrayOf("dev")
        is Alpha -> arrayOf("eap")
        is ReleaseCandidate -> arrayOf("rc")
        is Stable -> arrayOf()
    }

val tag: String?
    get() = getenvSafe("GITHUB_TAG")
        ?.takeUnless { tag -> tag.startsWith("refs/heads/") }
        ?.removePrefix("refs/tags/")

val commitSha: String?
    get() = getenvSafe("GITHUB_SHA")

val libraryVersion: Version
    get() = Version(tag, commitSha)

fun Version.toVersionName(): String =
    when (this) {
        is Snapshot -> commit?.let { sha -> "${sha.take(CommitHashLength)}-SNAPSHOT" } ?: "SNAPSHOT"
        is Alpha -> "${mainVersion.versionName}-alpha$alpha"
        is ReleaseCandidate -> "${mainVersion.versionName}-${alpha?.let { "alpha$it-" } ?: ""}rc${candidate}"
        is Stable -> mainVersion.versionName
    }

fun Version.toOssrhDeploymentUri(): URI =
    when (this) {
        is Snapshot -> URI("https://s01.oss.sonatype.org/content/repositories/snapshots/")
        is Alpha, is Stable, is ReleaseCandidate -> URI("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
    }

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

fun Project.installGitHooks() = afterEvaluate {
    projectHooksDir.listFiles { f -> f.extension == "sh" }
        ?.forEach { f ->
            val target = File(gitHooksDir, f.nameWithoutExtension)

            f.copyTo(target, overwrite = true)
            target.setExecutable(true, false)
        }
}

val Project.projectHooksDir: File
    get() = Paths.get(rootDir.path, "hooks").toFile()

val Project.gitHooksDir: File
    get() = Paths.get(rootDir.path, ".git", "hooks").toFile()

val Project.detektConfig: File
    get() = Paths.get(rootDir.path, "detekt", "detekt-config.yml").toFile()

val Project.detektBaseline: File
    get() = Paths.get(rootDir.path, "detekt", "detekt-baseline.xml").toFile()

fun Project.ciVariable(
    name: String,
): String? = getenvSafe(name) ?: getPropertySafe(name)

private fun Project.getPropertySafe(
    name: String,
): String? =
    properties[name]?.toString().takeUnless(CharSequence?::isNullOrEmpty)

fun getenvSafe(
    name: String,
): String? =
    System.getenv(name).takeUnless(CharSequence?::isNullOrEmpty)

internal val MajorMinorPatch.versionName
    get() = "${major}.${minor}.${patch}"
