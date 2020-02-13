package com.oliynick.max.elm.time.travel.app.domain.component

import com.oliynick.max.elm.core.component.Component
import com.oliynick.max.elm.core.component.effect
import com.oliynick.max.elm.core.component.with
import com.oliynick.max.elm.time.travel.app.domain.cms.*
import com.oliynick.max.elm.time.travel.app.domain.resolver.AppResolver
import com.oliynick.max.elm.time.travel.app.domain.resolver.HasChannels
import com.oliynick.max.elm.time.travel.app.domain.updater.*
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
                    is StoreFiles -> TODO()
                    is StoreServerSettings -> TODO()
                    is DoStartServer -> effect { NotifyStarted }
                    DoStopServer -> effect { NotifyStopped }
                    is DoApplyCommand -> TODO()
                    is DoApplyState -> TODO()
                    is DoNotifyOperationException -> TODO()
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
    HasChannels

@Suppress("FunctionName")
fun TestEnvironment(
    resolver: AppResolver<TestEnvironment>
): TestEnvironment =
    object : TestEnvironment,
             Updater<TestEnvironment> by LiveUpdater(),
             NotificationUpdater by LiveNotificationUpdater,
             UiUpdater by LiveUiUpdater,
             AppResolver<TestEnvironment> by resolver,
             HasChannels by HasChannels() {}