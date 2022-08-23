@file:Suppress("TestFunctionName")

package io.github.xlopec.tea.time.travel.plugin.environment

import androidx.compose.ui.test.IdlingResource
import io.github.xlopec.tea.time.travel.plugin.feature.notification.NotificationResolver
import io.github.xlopec.tea.time.travel.plugin.feature.server.ServerCommandResolver
import io.github.xlopec.tea.time.travel.plugin.feature.storage.StorageResolver
import io.github.xlopec.tea.time.travel.plugin.integration.AppResolver
import io.github.xlopec.tea.time.travel.plugin.integration.AppUpdater
import io.github.xlopec.tea.time.travel.plugin.integration.Environment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.TestCoroutineDispatcher

interface TestEnvironment : Environment, IdlingResource

fun TestEnvironment(
    serverCommandResolver: TestServerCommandResolver = SimpleTestServerCommandResolver(),
    storageResolver: TestStorageResolver = SimpleTestStorageResolver(),
    notificationResolver: NotificationResolver = SimpleTestNotificationResolver(),
    scope: CoroutineScope = CoroutineScope(TestCoroutineDispatcher()),
): TestEnvironment = object : TestEnvironment,
    AppUpdater by AppUpdater(),
    StorageResolver by storageResolver,
    ServerCommandResolver by serverCommandResolver,
    NotificationResolver by notificationResolver,
    AppResolver<Environment> by AppResolver(),
    CoroutineScope by scope {

    override val isIdleNow: Boolean
        get() = serverCommandResolver.isIdleNow && storageResolver.isIdleNow
}
