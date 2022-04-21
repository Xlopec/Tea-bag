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

@file:Suppress("FunctionName")

import java.net.URI

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

data class MajorMinorPatch(
    val major: Int,
    val minor: Int,
    val patch: Int,
)

/**
 * Denotes artifact version as per [sem ver](https://semver.org/) spec.
 * Currently it includes:
 * * Snapshot - `hash-SNAPSHOT` or just `SNAPSHOT`
 * * Alpha - `v1.2.3-alpha4`
 * * Release candidate (might be alpha RC) - `v1.2.3-rc5` or `v1.2.3-alpha4-rc5`
 * * Stable - `v1.2.3`
 */
sealed class Version

data class Snapshot(
    val commit: String?,
) : Version() {
    init {
        require(commit == null || commit.length >= CommitHashLength) { "Invalid hash: $commit" }
    }
}

data class Alpha(
    val value: String,
    val mainVersion: MajorMinorPatch,
    val alpha: Int,
) : Version() {
    companion object {
        fun fromTag(
            rawTag: String,
        ) = AlphaRegexp.groupValues(rawTag)
            .let { group -> Alpha(rawTag, group.toMajorMinorPatch(), group[4].toInt()) }
    }
}

data class ReleaseCandidate(
    val value: String,
    val mainVersion: MajorMinorPatch,
    val alpha: Int?,
    val candidate: Int,
) : Version() {
    val isAlphaRc = alpha != null

    companion object {
        fun fromTag(
            rawTag: String,
        ) = ReleaseCandidateRegexp.groupValues(rawTag)
            .let { group ->
                ReleaseCandidate(
                    rawTag,
                    group.toMajorMinorPatch(),
                    group[5].toIntOrNull(),
                    group[6].toInt()
                )
            }
    }
}

data class Stable(
    val value: String,
    val mainVersion: MajorMinorPatch,
) : Version() {
    companion object {
        fun fromTag(
            rawTag: String,
        ) = Stable(rawTag, StableRegexp.groupValues(rawTag).toMajorMinorPatch())
    }
}

fun Version(
    rawTag: String?,
    commit: String?
): Version =
    when {
        rawTag.isNullOrEmpty() -> Snapshot(commit)
        rawTag.matches(AlphaRegexp) -> Alpha.fromTag(rawTag)
        rawTag.matches(ReleaseCandidateRegexp) -> ReleaseCandidate.fromTag(rawTag)
        rawTag.matches(StableRegexp) -> Stable.fromTag(rawTag)
        else -> error(
            "Invalid tag: $rawTag, release tag should be absent or match any of the following regular " +
                    "expressions: ${
                        listOf(AlphaRegexp,
                            ReleaseCandidateRegexp,
                            StableRegexp).joinToString(transform = Regex::pattern)
                    }"
        )
    }

val Version.isSnapshot: Boolean get() = this is Snapshot

private fun Regex.groupValues(
    s: String,
) = (find(s) ?: error("Couldn't parse string '$s' for regex '$pattern'")).groupValues

private fun List<String>.toMajorMinorPatch() =
    MajorMinorPatch(this[1].toInt(), this[2].toInt(), this[3].toInt())

/**
 * Groups:
 * * 1 major, 123
 * * 2 minor, 123
 * * 3 patch, 123
 * * 4 alpha, 123
 */
private val AlphaRegexp = Regex("^v(\\d+)\\.(\\d+)\\.(\\d+)-alpha([1-9]\\d*)$")

/**
 * Groups:
 * * 1 major, 123
 * * 2 minor, 123
 * * 3 patch, 123
 * * 4 is alpha? -alpha123
 * * 5 rc number, 123
 */
private val ReleaseCandidateRegexp =
    Regex("^v(\\d+)\\.(\\d+)\\.(\\d)+(-alpha([1-9]\\d*))?-rc([1-9]\\d*)$")

/**
 * Groups:
 * * 1 major, 123
 * * 2 minor, 123
 * * 3 patch, 123
 */
private val StableRegexp = Regex("^v(\\d+)\\.(\\d+)\\.(\\d+)$")

fun Version.toVersionName(): String =
    when (this) {
        is Snapshot -> commit?.let { sha -> "${sha.take(CommitHashLength)}-SNAPSHOT" } ?: "SNAPSHOT"
        is Alpha -> "${mainVersion.versionName}-alpha$alpha"
        is ReleaseCandidate -> "${mainVersion.versionName}-${alpha?.let { "alpha$it-" } ?: ""}rc$candidate"
        is Stable -> mainVersion.versionName
    }

fun Version.toOssrhDeploymentUri(): URI =
    when (this) {
        is Snapshot -> URI("https://s01.oss.sonatype.org/content/repositories/snapshots/")
        is Alpha, is Stable, is ReleaseCandidate -> URI("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
    }

val MajorMinorPatch.versionName
    get() = "$major.$minor.$patch"
