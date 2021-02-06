/*
 * Copyright (C) 2021. Maksym Oliinyk.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
        val value: String
    ) : Tag()

    data class ReleaseCandidate(
        val value: String
    ) : Tag()

    data class Release(
        val value: String
    ) : Tag()
}

fun isLocalEnv(): Boolean = !isCiEnv()

fun isCiEnv(): Boolean =
    System.getenv("CI")?.toBoolean() == true

fun bintrayApiKey(): String? =
    if (isCiEnv()) System.getenv("BINTRAY_API_KEY") else null

fun pluginReleaseChannels(): Array<String> =
    when (tag()) {
        Tag.Develop -> arrayOf("dev")
        is Tag.Alpha -> arrayOf("eap")
        is Tag.ReleaseCandidate -> arrayOf("rc")
        is Tag.Release -> emptyArray()
    }

fun commitSha(): String? =
    System.getenv("GITHUB_SHA")
        .takeUnless(CharSequence?::isNullOrEmpty)

fun versionName(): String =
    when (val tag = tag()) {
        Tag.Develop -> commitSha()?.let { sha -> "DEV-${sha.take(CommitHashLength)}" } ?: "DEV"
        is Tag.Alpha -> tag.value
        is Tag.ReleaseCandidate -> tag.value
        is Tag.Release -> tag.value
    }

fun Project.installGitHooks() = afterEvaluate {
    (projectHooksDir.listFiles { f -> f.extension == "sh" } ?: emptyArray())
        .forEach { f ->
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
