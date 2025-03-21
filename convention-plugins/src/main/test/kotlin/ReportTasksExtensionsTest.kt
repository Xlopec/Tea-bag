import io.kotlintest.matchers.types.shouldBeNull
import io.kotlintest.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.io.File

@RunWith(JUnit4::class)
internal class ReportTasksExtensionsTest {

    @get:Rule
    internal var fileRule = TemporaryFolder()

    @Test
    fun `commonParentDir function returns correct result given there is common dir and path lengths same`() {
        val commonFolderPrefix = arrayOf("path", "to", "folder")
        val expectedCommonDir = fileRule.newFolder(*commonFolderPrefix)
        val dirA = fileRule.newFolder(*commonFolderPrefix, "a")
        val dirB = fileRule.newFolder(*commonFolderPrefix, "b")
        val actualCommonDir = dirA.commonParentDir(dirB)

        actualCommonDir shouldBe expectedCommonDir
    }

    @Test
    fun `commonParentDir function returns correct result given paths are same`() {
        val expectedCommonDir = fileRule.newFolder("path", "to", "folder")
        val actualCommonDir = expectedCommonDir.commonParentDir(expectedCommonDir)

        actualCommonDir shouldBe expectedCommonDir
    }

    @Test
    fun `commonParentDir function returns correct result given there is common dir and path lengths aren't same`() {
        val commonFolderPrefix = arrayOf("path", "to", "folder")
        val expectedCommonDir = fileRule.newFolder(*commonFolderPrefix)
        val dirA = fileRule.newFolder(*commonFolderPrefix, "a")
        val dirB = fileRule.newFolder(*commonFolderPrefix, "b", "c", "d")
        val actualCommonDir = dirA.commonParentDir(dirB)

        actualCommonDir shouldBe expectedCommonDir
    }

    @Test
    fun `when search for common parent dir function returns correct result given there is no common dir`() {
        val actualCommonDir = File("path/to/a").commonParentDir(File("some/another/path/to/b"))

        actualCommonDir.shouldBeNull()
    }
}
