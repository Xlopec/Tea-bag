import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.TestReport
import java.io.File

fun Test.configureOutputLocation(
    htmlDestinationDir: File,
    xmlDestinationDir: File,
) {

    val commonDir = htmlDestinationDir.commonParentDir(xmlDestinationDir)

    if (commonDir != null) {
        description = "$description Also copies test reports to $commonDir"
    }
    reports {
        html.outputLocation.set(htmlDestinationDir)
        junitXml.outputLocation.set(xmlDestinationDir)
    }
}

fun TestReport.configureOutputLocation(
    destinationDir: File,
) {
    description = "$description Also copies test reports to $destinationDir"
    this@configureOutputLocation.destinationDir = destinationDir
}

private fun File.commonParentDir(
    other: File
): File? {

    fun tryFindCommonDir(first: File, second: File): File? {
        if (first == second) return first

        return first.absolutePath.indexOf(second.absolutePath)
            .takeIf { i -> i >= 0 }
            ?.let { i -> File(first.absolutePath.substring(i, i + first.absolutePath.length)) }
    }

    return tryFindCommonDir(this, other) ?: tryFindCommonDir(other, this)
}
