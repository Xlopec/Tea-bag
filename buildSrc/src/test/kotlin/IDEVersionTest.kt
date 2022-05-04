import io.kotlintest.shouldBe
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
internal class IDEVersionTest {

    private companion object {
        private val LatestVersion = IDEVersion(Product.IC, 2022, 5)
        private val OldestVersion = IDEVersion(Product.IC, 2021, 1)

        private val TestIdeVersions = listOf(
            IDEVersion(Product.IC, 2021, 2),
            IDEVersion(Product.IC, 2022, 3),
            OldestVersion,
            IDEVersion(Product.IC, 2022, 1),
            LatestVersion,
            IDEVersion(Product.IC, 2022, 4),
            IDEVersion(Product.IC, 2021, 3),
        )
    }

    @Test
    fun `when search for latest version, then correct version returned`() {
        TestIdeVersions.latest() shouldBe LatestVersion
    }

    @Test
    fun `when search for oldest version, then correct version returned`() {
        TestIdeVersions.oldest() shouldBe OldestVersion
    }

    @Test
    fun `when convert to version name, then correct version name returned`() {
        LatestVersion.versionName shouldBe "IC-2022.5"
    }

    @Test
    fun `when convert to build number, then correct build number returned`() {
        LatestVersion.buildNumber shouldBe "225"
    }
}
