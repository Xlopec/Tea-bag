package io.github.xlopec.tea.compose

import androidx.compose.runtime.InternalComposeApi
import androidx.compose.runtime.key
import io.github.xlopec.tea.compose.Cmd.DoLoad
import io.github.xlopec.tea.core.Component
import io.github.xlopec.tea.core.ExperimentalTeaApi
import io.github.xlopec.tea.core.Initializer
import io.github.xlopec.tea.core.Resolver
import io.github.xlopec.tea.core.Sink
import io.github.xlopec.tea.core.command
import io.github.xlopec.tea.core.noCommand
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.jvm.JvmInline
import kotlin.reflect.KClass
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime

internal interface Screen {
    val id: KClass<out Screen>
        get() = this::class
}

@JvmInline
internal value class FirstScreen(val data: String? = null) : Screen
internal data object SecondScreen : Screen
internal data object ThirdScreen : Screen
internal data object FourthScreen : Screen

internal sealed interface Msg {
    data class LoadData(val screenId: KClass<out Screen>) : Msg
    data class OnDataLoaded(val screenId: KClass<out Screen>, val data: String) : Msg
    data object Pop : Msg
}

internal sealed interface Cmd {
    data class DoLoad(val screenId: KClass<out Screen>, val delay: Duration) : Cmd

    @JvmInline
    value class DoLog(val message: String) : Cmd
}

internal data class App(
    val stack: List<Screen>,
)

@OptIn(ExperimentalTeaApi::class)
private fun MakeComponent(
    scope: CoroutineScope,
    app: App,
    resolver: Resolver<Msg, App, Cmd>
): Component<Msg, App, Cmd> {
    return Component(
        initializer = Initializer(app),
        resolver = resolver,
        updater = { message, state ->
            when (message) {
                is Msg.LoadData -> state command DoLoad(message.screenId, 300.milliseconds)
                Msg.Pop -> state.copy(stack = state.stack.drop(1)).noCommand()
                is Msg.OnDataLoaded -> {
                    val i = state.stack.indexOfFirst { it.id == message.screenId }

                    if (i < 0) {
                        state.noCommand()
                    } else {
                        state.copy(
                            stack = state.stack.mapIndexed { index, screen -> if (index == i) FirstScreen(message.data) else screen }
                        ).noCommand()
                    }
                }
            }
        },
        scope = scope,
    )
}

/**
 * Entry point for the sample application.
 */
@OptIn(ExperimentalTime::class, InternalComposeApi::class, ExperimentalTeaApi::class)
public fun main(): Unit = runBlocking {

    val app = App(
        listOf(
            FirstScreen(),
            SecondScreen,
            ThirdScreen,
            FourthScreen,
        )
    )

    val component = MakeComponent(
        scope = this,
        app = app,
        resolver = { snapshots, context ->
            ComposeResolver(
                scope = context,
                snapshots = snapshots,
                clockPolicy = ClockPolicy.Internal,
                snapshotManagerPolicy = SnapshotNotifierPolicy.WhileActive
            ) { state, commands ->
                for (screen in state.stack) {
                    key(screen.id) {
                        TrackingEffect(screen.id) {
                            commands
                                .filter { cmd ->
                                    screen.id == (cmd as? DoLoad)?.screenId
                                }
                                .collect { cmd ->
                                    resolve(cmd, context, this)
                                }
                        }
                    }
                }

                TrackingEffect(Unit) {
                    commands
                        .filter { cmd ->
                            cmd !is DoLoad
                        }
                        .collect { cmd ->
                            resolve(cmd, context, this)
                        }
                }
            }
        }
    )

    launch {
        component(
            flowOf(
                Msg.LoadData(FirstScreen::class),
                Msg.Pop,
            )
                .onEach {
                    delay(18.milliseconds)
                }
        )
            .collect {
                println(it)
            }
    }
}

internal fun resolve(cmd: Cmd, sink: Sink<Msg>, scope: TrackingScope) {
    println("Resolving command: $cmd")

    when (cmd) {
        is DoLoad -> scope.launchSingle(cmd.screenId) {
            try {
                println("Loading data for $cmd")
                delay(cmd.delay)
                sink(Msg.OnDataLoaded(cmd.screenId, "Hello from Tea library"))
            } finally {
                println("Done for $cmd")
            }
        }

        is Cmd.DoLog -> println("Log: ${cmd.message}")
    }
}
