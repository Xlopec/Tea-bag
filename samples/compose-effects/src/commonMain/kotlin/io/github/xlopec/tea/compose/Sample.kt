/*
 * MIT License
 *
 * Copyright (c) 2026. Maksym Oliinyk.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

@file:Suppress("FunctionName")

package io.github.xlopec.tea.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import io.github.xlopec.tea.compose.Command.DoLoad
import io.github.xlopec.tea.core.Component
import io.github.xlopec.tea.core.Initializer
import io.github.xlopec.tea.core.Resolver
import io.github.xlopec.tea.core.Sink
import io.github.xlopec.tea.core.Snapshot
import io.github.xlopec.tea.core.command
import io.github.xlopec.tea.core.noCommand
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.jvm.JvmInline
import kotlin.reflect.KClass
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Represents a screen in the application.
 */
internal interface Screen {
    /**
     * Unique identifier for the screen, based on its class.
     */
    val id: KClass<out Screen>
        get() = this::class
}

/**
 * The first screen, which can optionally hold some data.
 */
@JvmInline
internal value class FirstScreen(val data: String? = null) : Screen

/**
 * A simple data object representing the second screen.
 */
internal data object SecondScreen : Screen

/**
 * A simple data object representing the third screen.
 */
internal data object ThirdScreen : Screen

/**
 * A simple data object representing the fourth screen.
 */
internal data object FourthScreen : Screen

/**
 * Represents all possible messages (events) that can occur in the application.
 * These messages are processed by the updater to produce a new state.
 */
internal sealed interface Message {
    /**
     * Message to start loading data for a specific screen.
     */
    data class LoadData(val screenId: KClass<out Screen>) : Message

    /**
     * Message sent when data loading for a screen is complete.
     */
    data class OnDataLoaded(val screenId: KClass<out Screen>, val data: String) : Message

    /**
     * Message to remove the top screen from the stack.
     */
    data object Pop : Message
}

/**
 * Represents side effects or commands to be executed outside of the updater.
 * Commands are handled by the resolver.
 */
internal sealed interface Command {
    /**
     * Command to perform an asynchronous data loading operation.
     */
    data class DoLoad(val screenId: KClass<out Screen>, val delay: Duration) : Command

    /**
     * Command to log a message.
     */
    @JvmInline
    value class DoLog(val message: String) : Command
}

/**
 * The application state, consisting of a stack of screens.
 */
internal data class App(
    val stack: List<Screen>,
)

/**
 * Creates the core component of the application.
 *
 * @param scope the [CoroutineScope] in which the component will operate.
 * @param app the initial state of the application.
 * @param resolver the resolver responsible for handling commands.
 * @return a [Component] that manages the application's state machine.
 */
private fun AppComponent(
    scope: CoroutineScope,
    app: App,
    resolver: Resolver<Message, App, Command>,
): Component<Message, App, Command> {
    return Component(
        initializer = Initializer(app),
        resolver = resolver,
        updater = { message, state ->
            // The updater is a pure function that takes the current state and a message,
            // and returns a new state along with optional commands to execute.
            when (message) {
                is Message.LoadData -> state command DoLoad(message.screenId, 300.milliseconds)
                Message.Pop -> state.copy(stack = state.stack.drop(1)).noCommand()
                is Message.OnDataLoaded -> {
                    val i = state.stack.indexOfFirst { it.id == message.screenId }

                    if (i < 0) {
                        state.noCommand()
                    } else {
                        state.copy(
                            stack = state.stack.mapIndexed { index, screen -> if (index == i) FirstScreen(message.data) else screen },
                        ).noCommand()
                    }
                }
            }
        },
        scope = scope,
    )
}

/**
 * The main entry point for the sample application.
 * It demonstrates how to set up the TEA component with a Compose resolver,
 */
fun main(): Unit = runBlocking {

    val app = App(
        listOf(
            FirstScreen(),
            SecondScreen,
            ThirdScreen,
            FourthScreen,
        ),
    )

    val component = AppComponent(
        scope = this,
        app = app,
        resolver = { snapshots ->
            // ComposeResolver is used here to manage effects in a way that integrates
            // with Jetpack Compose's lifecycle (using TrackingEffect).
            ComposeResolver(
                scope = contextOf<CoroutineScope>(),
                clockPolicy = ClockPolicy.Internal,
                snapshotManagerPolicy = SnapshotNotifierPolicy.WhileActive,
            ) {
                val snapshot by snapshots.collectAsState(null)
                val state = snapshot?.currentState

                if (state != null) {
                    val commands = snapshots.rememberCommands()

                    for (screen in state.stack) {
                        key(screen.id) {
                            // TrackingEffect allows grouping command execution by a key (screen.id),
                            // ensuring that commands for a specific screen are handled in its own scope.
                            TrackingEffect(screen.id) {
                                commands
                                    .filter { cmd ->
                                        screen.id == (cmd as? DoLoad)?.screenId
                                    }
                                    .collect { cmd ->
                                        resolve(cmd)
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
                                resolve(cmd)
                            }
                    }
                }
            }
        },
    )

    launch {
        // Dispatching initial messages to the component.
        component(
            flowOf(
                Message.LoadData(FirstScreen::class),
                Message.Pop,
            )
                .onEach {
                    // some snapshots might be skipped if published too frequently,
                    // see [androidx.compose.runtime.produceState] for more info
                    delay(18.milliseconds)
                },
        )
            .collect {
                // Observe and print state snapshots as they are produced.
                println(it)
            }
    }
}

/**
 * Resolves a single command by performing the associated side effect.
 *
 * @param command the command to resolve.
 * @param sink a function to send messages back to the component.
 * @param scope the [TrackingScope] used to launch asynchronous tasks.
 */
context(sink: Sink<Message>, scope: TrackingScope)
internal fun resolve(command: Command) {
    println("Resolving command: $command")

    when (command) {
        is DoLoad -> scope.launchSingle(command.screenId) {
            try {
                println("Loading data for $command")
                delay(command.delay)
                sink(Message.OnDataLoaded(command.screenId, "Hello from Tea library"))
            } finally {
                println("Done for $command")
            }
        }

        is Command.DoLog -> println("Log: ${command.message}")
    }
}

@Composable
private fun Flow<Snapshot<*, *, Command>>.rememberCommands(): Flow<Command> {
    return remember(this) {
        transform { snapshot -> snapshot.commands.forEach { emit(it) } }
    }
}
