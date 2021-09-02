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

import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
internal class VersionTest {

    private companion object {
        const val Major = 1
        const val Minor = 2
        const val Patch = 3
        const val Alpha = 4
        const val RC = 5
        val MajorMinorPatch = MajorMinorPatch(Major, Minor, Patch)
        const val CommitHash = "d41d8cd98f00b204e9800998ecf8427e"
    }

    @Test
    fun `when convert to version, given snapshot version, then parsed as snapshot`() {
        Version(null, null) shouldBe Snapshot(null)
        Version("", null) shouldBe Snapshot(null)
        Version("", CommitHash) shouldBe Snapshot(CommitHash)
    }

    @Test
    fun `when convert to version, given alpha version, then parsed as alpha`() {
        val tag = "v$Major.$Minor.$Patch-alpha$Alpha"

        Version(tag, null) shouldBe Alpha(tag, MajorMinorPatch, Alpha)
    }

    @Test
    fun `when convert to version, given alpha RC version, then parsed as alpha RC`() {
        val tag = "v$Major.$Minor.$Patch-alpha$Alpha-rc$RC"

        Version(tag, null) shouldBe ReleaseCandidate(tag, MajorMinorPatch, Alpha, RC)
    }

    @Test
    fun `when convert to version, given RC version, then parsed as RC`() {
        val tag = "v$Major.$Minor.$Patch-rc$RC"
        Version(tag, null) shouldBe ReleaseCandidate(tag, MajorMinorPatch, null, RC)
    }

    @Test
    fun `when convert to version, given stable version, then parsed as stable`() {
        val tag = "v$Major.$Minor.$Patch"
        Version(tag, null) shouldBe Stable(tag, MajorMinorPatch)
    }

    @Test
    fun `when convert to version, given invalid version, then parse exception thrown`() {
        shouldThrow<IllegalStateException> {
            Version("$Major.$Minor.$Patch", null)
        }
    }
}
