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

import io.kotlintest.Failures
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
internal class ToVersionNameExtensionTest {

    private companion object {
        const val LongCommitHash = "d41d8cd98f00b204e9800998ecf8427e"
        val ShortCommitHash = LongCommitHash.take(CommitHashLength)
    }

    // Snapshot tests

    @Test
    fun `when convert to version name, given snapshot version and long hash, then short hash used`() =
        Snapshot(LongCommitHash).toVersionName() shouldBe "$ShortCommitHash-SNAPSHOT"

    @Test
    fun `when convert to version name, given snapshot version and null hash, then no hash used`() =
        Snapshot(null).toVersionName() shouldBe "SNAPSHOT"

    @Test
    fun `when convert to version name, given snapshot version and empty hash, then exception thrown`() {
        shouldThrow<IllegalArgumentException> {
            Snapshot("").toVersionName()
        }
    }

    // Alpha tests

    @Test
    fun `when convert to version name, given parse alpha version from tag, then it's correct`() =
        Alpha.fromTag("v1.2.3-alpha4").toVersionName() shouldBe "1.2.3-alpha4"

    @Test
    fun `when convert to version name, given parse alpha version from invalid tag, then parse exception thrown`() =
        shouldThrowForEach<IllegalStateException>(
            Alpha::fromTag,
            "v1.2.3-alpha4-rc56",
            "1.2.3-alpha4-rc56",
            "v1.2.3alpha4-rc56",
            "v1.2.3-56",
        )

    // RC tests

    @Test
    fun `when convert to version name, given parse alpha RC version from tag, then it's correct`() =
        ReleaseCandidate.fromTag("v1.2.3-alpha4-rc56").toVersionName() shouldBe "1.2.3-alpha4-rc56"

    @Test
    fun `when convert to version name, given parse RC version from tag, then it's correct`() =
        ReleaseCandidate.fromTag("v1.2.3-rc45").toVersionName() shouldBe "1.2.3-rc45"

    @Test
    fun `when convert to version name, given parse RC version from invalid tag, then parse exception thrown`() =
        shouldThrowForEach<IllegalStateException>(
            ReleaseCandidate::fromTag,
            "v1.2.3",
            "1.2.3",
            "v1.2.3-r1",
            "v1.2.3rc1",
            "v1.2.3-rc01",
        )

    // Release tests

    @Test
    fun `when convert to version name, given parse release version from tag, then it's correct`() =
        Stable.fromTag("v1.2.3").toVersionName() shouldBe "1.2.3"

    @Test
    fun `when convert to version name, given parse release version from invalid tag, then parse exception thrown`() =
        shouldThrowForEach<IllegalStateException>(Stable::fromTag, "v1.2.3-rc1", "1.2.3")
}

// todo rewrite using kotest property tests
private inline fun <reified T : Throwable> shouldThrowForEach(
    constructor: (String) -> Version,
    vararg args: String,
) {
    val fails = args.fold(ArrayList<Pair<String, Any>>(args.size)) { fails, arg ->

        runCatching { constructor(arg) }
            .fold(onSuccess = { arg to it }, onFailure = { if (it is T) null else arg to it })
            ?.also { fails += it }

        fails
    }

    val message = fails.joinToString(separator = ",\n") { (arg, res) ->
        if (res is Throwable) {
            "wanted exception of type ${T::class} but was $res\n"
        } else {
            "test case didn't throw an exception for argument \"$arg\" -> $res\n"
        }
    }

    if (message.isNotEmpty()) {
        throw Failures.failure(message)
    }
}
