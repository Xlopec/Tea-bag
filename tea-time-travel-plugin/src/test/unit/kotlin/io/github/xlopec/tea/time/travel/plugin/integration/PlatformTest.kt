package io.github.xlopec.tea.time.travel.plugin.integration

import com.intellij.openapi.project.IndexNotReadyException
import io.github.xlopec.tea.time.travel.plugin.model.Type
import io.github.xlopec.tea.time.travel.plugin.util.LoggerStub
import io.github.xlopec.tea.time.travel.plugin.util.ProjectStub
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertIs
import kotlin.test.assertNull

class PlatformTest {

    @Test
    fun `test psiClassFor returns null and logs exception when exception occurs internally`() = runTest {
        val project = object : ProjectStub() {
            override fun <T> getService(p0: Class<T>): T = throw IndexNotReadyException.create()
        }
        val logger = object : LoggerStub() {
            var th: Throwable? = null

            override fun warn(p0: String?, p1: Throwable?) {
                require(th == null) { "Test shouldn't produce more then 1 exception, was $p1, recorded $th" }
                th = p1
            }
        }

        val platform = Platform(project, logger)
        val psiClass = platform.psiClassFor(Type.of("java.util.StringJoiner"))

        assertNull(psiClass)
        assertIs<IndexNotReadyException>(logger.th)
    }
}
