package io.github.xlopec.tea.time.travel.plugin.environment

import androidx.compose.ui.test.IdlingResource
import io.github.xlopec.tea.data.Either
import io.github.xlopec.tea.data.Left
import io.github.xlopec.tea.time.travel.plugin.data.TestComponentId
import io.github.xlopec.tea.time.travel.plugin.data.TestUserValue
import io.github.xlopec.tea.time.travel.plugin.feature.notification.ComponentImported
import io.github.xlopec.tea.time.travel.plugin.feature.notification.OperationException
import io.github.xlopec.tea.time.travel.plugin.feature.storage.DoExportSessions
import io.github.xlopec.tea.time.travel.plugin.feature.storage.DoImportSession
import io.github.xlopec.tea.time.travel.plugin.feature.storage.DoStoreSettings
import io.github.xlopec.tea.time.travel.plugin.feature.storage.StorageResolver
import io.github.xlopec.tea.time.travel.plugin.integration.NotificationMessage
import io.github.xlopec.tea.time.travel.plugin.integration.StoreCommand
import io.github.xlopec.tea.time.travel.plugin.model.DebuggableComponent

interface TestStorageResolver : StorageResolver, IdlingResource
class SimpleTestStorageResolver : TestStorageResolver {
    override suspend fun resolveStoreCommand(command: StoreCommand): Either<NotificationMessage?, OperationException> =
        when (command) {
            is DoExportSessions -> Left(null)
            is DoImportSession -> Left(ComponentImported(DebuggableComponent(TestComponentId, TestUserValue)))
            is DoStoreSettings -> Left(null)
            else -> error("can't get here")
        }

    override val isIdleNow: Boolean = true
}
