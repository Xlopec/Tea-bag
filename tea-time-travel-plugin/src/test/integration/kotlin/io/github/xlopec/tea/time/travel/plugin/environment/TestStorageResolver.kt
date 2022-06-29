package io.github.xlopec.tea.time.travel.plugin.environment

import androidx.compose.ui.test.IdlingResource
import io.github.xlopec.tea.core.ResolveCtx
import io.github.xlopec.tea.core.effect
import io.github.xlopec.tea.time.travel.plugin.data.TestComponentId1
import io.github.xlopec.tea.time.travel.plugin.data.TestUserValue
import io.github.xlopec.tea.time.travel.plugin.feature.notification.ComponentImported
import io.github.xlopec.tea.time.travel.plugin.feature.storage.DoExportSessions
import io.github.xlopec.tea.time.travel.plugin.feature.storage.DoImportSession
import io.github.xlopec.tea.time.travel.plugin.feature.storage.DoStoreSettings
import io.github.xlopec.tea.time.travel.plugin.feature.storage.StorageResolver
import io.github.xlopec.tea.time.travel.plugin.integration.Message
import io.github.xlopec.tea.time.travel.plugin.integration.StoreCommand
import io.github.xlopec.tea.time.travel.plugin.model.DebuggableComponent

interface TestStorageResolver : StorageResolver, IdlingResource
class SimpleTestStorageResolver : TestStorageResolver {
    override fun resolveStoreCommand(
        command: StoreCommand,
        ctx: ResolveCtx<Message>,
    ) {
        ctx.effect {
            when (command) {
                is DoExportSessions -> null
                is DoImportSession -> ComponentImported(DebuggableComponent(TestComponentId1, TestUserValue))
                is DoStoreSettings -> null
                else -> error("can't get here")
            }
        }
    }

    override val isIdleNow: Boolean = true
}
