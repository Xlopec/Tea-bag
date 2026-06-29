/*
 * MIT License
 *
 * Copyright (c) 2026. Maksym Oliinyk.
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

import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.TestReport
import java.io.File

fun Test.configureOutputLocation(
    htmlDestinationDir: Provider<out Directory>,
    xmlDestinationDir: Provider<out Directory>,
) {

    val commonDir = htmlDestinationDir.get().asFile.commonParentDir(xmlDestinationDir.get().asFile)

    if (commonDir != null) {
        description = "$description Also copies test reports to $commonDir"
    }
    reports {
        html.outputLocation.set(htmlDestinationDir)
        junitXml.outputLocation.set(xmlDestinationDir)
    }
}

fun TestReport.configureOutputLocation(
    destinationDir: Provider<out Directory>,
) {
    description = "$description Also copies test reports to $destinationDir"
    destinationDirectory.set(destinationDir)
}

internal fun File.commonParentDir(
    other: File,
): File? {
    var first = IterableFile(this)
    var second = IterableFile(other)

    while (first != second && first.hasMore && second.hasMore) {
        when {
            first.segments == second.segments -> {
                first--
                second--
            }
            first.segments > second.segments -> first--
            else -> second--
        }
    }

    return first.takeIf { first == second }?.file
}

private class IterableFile(
    file: File,
) {
    var file: File = file
        private set
    var segments = file.absolutePath.split(File.separatorChar).size
        private set
    val hasMore: Boolean
        get() = file.parentFile != null

    operator fun dec(): IterableFile {
        file = file.parentFile ?: return this
        segments--
        return this
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IterableFile

        if (file != other.file) return false
        if (segments != other.segments) return false

        return true
    }

    override fun hashCode(): Int {
        var result = file.hashCode()
        result = 31 * result + segments.hashCode()
        return result
    }

    override fun toString(): String {
        return "IterableFile(file=$file, segments=$segments)"
    }
}
