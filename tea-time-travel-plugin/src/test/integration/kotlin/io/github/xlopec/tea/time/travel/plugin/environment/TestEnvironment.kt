@file:Suppress("TestFunctionName")

package io.github.xlopec.tea.time.travel.plugin.environment

import androidx.compose.ui.test.IdlingResource
import com.intellij.openapi.project.Project
import io.github.xlopec.tea.data.Either
import io.github.xlopec.tea.time.travel.plugin.feature.notification.NotificationResolver
import io.github.xlopec.tea.time.travel.plugin.feature.notification.OperationException
import io.github.xlopec.tea.time.travel.plugin.feature.server.ServerCommandResolver
import io.github.xlopec.tea.time.travel.plugin.feature.storage.StorageResolver
import io.github.xlopec.tea.time.travel.plugin.integration.AppResolver
import io.github.xlopec.tea.time.travel.plugin.integration.AppUpdater
import io.github.xlopec.tea.time.travel.plugin.integration.Environment
import io.github.xlopec.tea.time.travel.plugin.integration.NotificationMessage
import io.github.xlopec.tea.time.travel.plugin.integration.ServerCommand
import io.github.xlopec.tea.time.travel.plugin.integration.StoreCommand
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

interface TestEnvironment : Environment, IdlingResource

fun TestEnvironment(
    project: Project = TestProject()
): TestEnvironment = object : TestEnvironment,
    AppUpdater by AppUpdater(),
    StorageResolver by TestStorageResolver(),
    ServerCommandResolver by TestServerCommandResolver(),
    NotificationResolver by NotificationResolver(project),
    AppResolver<Environment> by AppResolver(),
    CoroutineScope by CoroutineScope(SupervisorJob()) {
    override val isIdleNow: Boolean
        get() = true
}

class TestServerCommandResolver : ServerCommandResolver {
    override suspend fun resolveServerCommand(command: ServerCommand): Either<NotificationMessage?, OperationException> {
        TODO("Not yet implemented")
    }
}

class TestStorageResolver : StorageResolver {
    override suspend fun resolveStoreCommand(command: StoreCommand): Either<NotificationMessage?, OperationException> {
        TODO("Not yet implemented")
    }
}
