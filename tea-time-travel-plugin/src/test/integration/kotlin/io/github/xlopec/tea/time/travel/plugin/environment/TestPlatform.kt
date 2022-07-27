package io.github.xlopec.tea.time.travel.plugin.environment

import com.intellij.psi.PsiClass
import io.github.xlopec.tea.time.travel.plugin.feature.storage.ExportSessions
import io.github.xlopec.tea.time.travel.plugin.integration.Platform
import io.github.xlopec.tea.time.travel.plugin.model.Type
import io.github.xlopec.tea.time.travel.protocol.ComponentId
import kotlinx.collections.immutable.ImmutableSet
import java.io.File

class TestPlatform : Platform {

    override suspend fun chooseSessionFile(): File {
        TODO("Not yet implemented")
    }

    override suspend fun chooseExportSessionDirectory(ids: ImmutableSet<ComponentId>): ExportSessions? {
        TODO("Not yet implemented")
    }

    override fun psiClassFor(type: Type): PsiClass? {
        TODO("Not yet implemented")
    }

    override fun navigateToSources(psiClass: PsiClass) {
        TODO("Not yet implemented")
    }

    override fun navigateToSettings() {
        TODO("Not yet implemented")
    }
}
