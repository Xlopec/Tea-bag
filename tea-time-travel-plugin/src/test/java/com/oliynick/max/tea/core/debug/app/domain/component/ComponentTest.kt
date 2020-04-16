package com.oliynick.max.tea.core.debug.app.domain.component

import com.oliynick.max.tea.core.component.Component
import com.oliynick.max.tea.core.component.with
import com.oliynick.max.tea.core.debug.app.component.cms.DoApplyMessage
import com.oliynick.max.tea.core.debug.app.component.cms.DoApplyState
import com.oliynick.max.tea.core.debug.app.component.cms.DoNotifyComponentAttached
import com.oliynick.max.tea.core.debug.app.component.cms.DoNotifyOperationException
import com.oliynick.max.tea.core.debug.app.component.cms.DoStartServer
import com.oliynick.max.tea.core.debug.app.component.cms.DoStopServer
import com.oliynick.max.tea.core.debug.app.component.cms.DoStoreSettings
import com.oliynick.max.tea.core.debug.app.component.cms.DoWarnUnacceptableMessage
import com.oliynick.max.tea.core.debug.app.component.cms.PluginCommand
import com.oliynick.max.tea.core.debug.app.component.cms.PluginMessage
import com.oliynick.max.tea.core.debug.app.component.cms.PluginState
import com.oliynick.max.tea.core.debug.app.component.cms.StartServer
import com.oliynick.max.tea.core.debug.app.component.cms.StopServer
import com.oliynick.max.tea.core.debug.app.component.resolver.AppResolver
import com.oliynick.max.tea.core.debug.app.component.resolver.HasMessageChannel
import com.oliynick.max.tea.core.debug.app.component.resolver.HasMessagesChannel
import com.oliynick.max.tea.core.debug.app.component.updater.LiveNotificationUpdater
import com.oliynick.max.tea.core.debug.app.component.updater.LiveUiUpdater
import com.oliynick.max.tea.core.debug.app.component.updater.LiveUpdater
import com.oliynick.max.tea.core.debug.app.component.updater.NotificationUpdater
import com.oliynick.max.tea.core.debug.app.component.updater.UiUpdater
import com.oliynick.max.tea.core.debug.app.component.updater.Updater
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ComponentTest {

    @Test(timeout = 500L)
    @Ignore
    fun test() = runBlocking {

        val resolver = object : AppResolver<TestEnvironment> {
            override suspend fun TestEnvironment.resolve(command: PluginCommand): Set<PluginMessage> =
                when (command) {
                    is DoStoreSettings -> TODO()
                    is DoStartServer -> TODO()//effect { NotifyStarted }
                    is DoStopServer -> TODO()//effect { NotifyStopped }
                    is DoApplyMessage -> TODO()
                    is DoApplyState -> TODO()
                    is DoNotifyOperationException -> TODO()
                    is DoWarnUnacceptableMessage -> TODO()
                    is DoNotifyComponentAttached -> TODO()
                }

        }

        val component = TestEnvironment(
            resolver
        ).PluginComponent().with { println(it) }

        val messages = Channel<PluginMessage>()

        launch {
            component.invoke(messages.consumeAsFlow()).collect()
        }

        messages.send(StartServer)

        messages.send(StopServer)
    }

    fun TestEnvironment.PluginComponent(): Component<PluginMessage, PluginState, PluginCommand> {

        suspend fun resolve(c: PluginCommand) = this.resolve(c)

        fun update(
            message: PluginMessage,
            state: PluginState
        ) = this.update(message, state)

        return TODO()//Component(Initializer(Stopped(Settings(ServerSettings("local", 8080U)))), ::resolve, ::update)
    }

}

interface TestEnvironment :
    Updater<TestEnvironment>,
    NotificationUpdater,
    UiUpdater,
    AppResolver<TestEnvironment>,
    HasMessageChannel

@Suppress("FunctionName")
fun TestEnvironment(
    resolver: AppResolver<TestEnvironment>
): TestEnvironment =
    object : TestEnvironment,
        Updater<TestEnvironment> by LiveUpdater(),
        NotificationUpdater by LiveNotificationUpdater,
        UiUpdater by LiveUiUpdater,
        AppResolver<TestEnvironment> by resolver,
        HasMessageChannel by HasMessagesChannel() {}