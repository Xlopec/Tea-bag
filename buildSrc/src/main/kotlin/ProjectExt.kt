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
import java.io.File
import java.nio.file.Paths

private const val CommitHashLength = 6
private val AlphaRegexp = Regex("v\\d+\\.\\d+\\.\\d+-alpha[1-9]\\d*")
private val ReleaseCandidateRegexp = Regex("v\\d+\\.\\d+\\.\\d+(-alpha[1-9]\\d*)?-rc[1-9]\\d*")
private val ReleaseRegexp = Regex("v\\d+\\.\\d+\\.\\d+")

private sealed class Tag {

    object Develop : Tag()

    data class Alpha(
        val value: String,
    ) : Tag()

    data class ReleaseCandidate(
        val value: String,
    ) : Tag()

    data class Release(
        val value: String,
    ) : Tag()
}

val isCiEnv: Boolean
    get() = getEnvSafe("CI")?.toBoolean() == true

val pluginReleaseChannels: Array<String>
    get() = when (tag()) {
        Tag.Develop -> arrayOf("dev")
        is Tag.Alpha -> arrayOf("eap")
        is Tag.ReleaseCandidate -> arrayOf("rc")
        is Tag.Release -> emptyArray()
    }

val commitSha: String?
    get() = getEnvSafe("GITHUB_SHA")

val versionName: String
    get() = when (val tag = tag()) {
        Tag.Develop -> commitSha?.let { sha -> "DEV-${sha.take(CommitHashLength)}" } ?: "DEV"
        is Tag.Alpha -> tag.value
        is Tag.ReleaseCandidate -> tag.value
        is Tag.Release -> tag.value
    }

val ossrhUser: String?
    get() = getEnvSafe("OSSRH_USER")
        .also { s -> checkCiPropertyValid("OSSRH_USER", s) }

val ossrhPassword: String?
    get() = getEnvSafe("OSSRH_PASSWORD")
        .also { s -> checkCiPropertyValid("OSSRH_PASSWORD", s) }

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

private fun tag(): Tag {
    val rawTag = System.getenv("GITHUB_TAG")
        .takeUnless(CharSequence?::isNullOrEmpty)
        ?.takeUnless { tag -> tag.startsWith("refs/heads/") }
        ?.removePrefix("refs/tags/")

    return when {
        rawTag.isNullOrEmpty() -> Tag.Develop
        rawTag.matches(AlphaRegexp) -> Tag.Alpha(rawTag)
        rawTag.matches(ReleaseCandidateRegexp) -> Tag.ReleaseCandidate(rawTag)
        rawTag.matches(ReleaseRegexp) -> Tag.Release(rawTag)
        else -> error(
            "Invalid tag: $rawTag, release tag should be absent or match any of the following regular " +
                    "expressions: ${
                        listOf(AlphaRegexp,
                            ReleaseCandidateRegexp,
                            ReleaseRegexp).joinToString(transform = Regex::pattern)
                    }"
        )
    }
}

private fun getEnvSafe(
    name: String,
): String? =
    System.getenv(name).takeUnless(CharSequence?::isNullOrEmpty)

private fun checkCiPropertyValid(name: String, value: String?) =
    check(isCiEnv && !value.isNullOrEmpty()) { "\"$name\" is null" }