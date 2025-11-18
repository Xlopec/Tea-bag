package io.github.xlopec.tea.compose

import androidx.compose.runtime.InternalComposeApi
import androidx.compose.runtime.key
import io.github.xlopec.tea.core.Component
import io.github.xlopec.tea.core.ExperimentalTeaApi
import io.github.xlopec.tea.core.Initializer
import io.github.xlopec.tea.core.Resolver
import io.github.xlopec.tea.core.Sink
import io.github.xlopec.tea.core.command
import io.github.xlopec.tea.core.noCommand
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.time.ExperimentalTime

internal interface Screen

internal data object FirstScreen : Screen // FeatureA

internal data object SecondScreen : Screen // FeatureB
internal data object ThirdScreen : Screen // FeatureB
internal data object FourthScreen : Screen // FeatureA
internal interface Cmd

internal interface ScreenCmd : Cmd {
    public val id: Any
        get() = FirstScreen::class.simpleName!!
}

internal sealed interface FeatureACmd : ScreenCmd {
    data object SubACmd1 : FeatureACmd
    data object SubACmd2 : FeatureACmd
    data object SubACmd3 : FeatureACmd
}

internal sealed interface FeatureBCmd : ScreenCmd {
    data object SubBCmd1 : FeatureACmd
    data object SubBCmd2 : FeatureACmd
    data object SubBCmd3 : FeatureACmd
}

internal data object GlobalAppCmd : Cmd

internal data object RmAppCmd : Cmd
internal data class DummyApp(
    val stack: List<Screen>
)

@OptIn(ExperimentalTeaApi::class)
private fun MakeComponent(
    scope: CoroutineScope,
    app: DummyApp,
    resolver: Resolver<String, DummyApp, Cmd>
): Component<String, DummyApp, Cmd> {
    return Component(
        initializer = Initializer(
            app
        ),
        resolver = resolver,
        updater = { m, s ->
            when (m) {
                "A" -> s command FeatureACmd.SubACmd1
                "B" -> s command FeatureBCmd.SubBCmd1
                "C" -> s command FeatureBCmd.SubBCmd2
                "R" -> s.copy(stack = s.stack.drop(1)).noCommand()
                else -> s.noCommand()
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

    val app = DummyApp(
        listOf(
            FirstScreen,
            SecondScreen,
            ThirdScreen,
            FourthScreen
        )
    )

    val component = MakeComponent(
        scope = this,
        app = app,
        resolver = { snapshots, context ->
            ComposeResolver(context, snapshots) { state, commands ->
                for (screen in state.stack) {
                    val id = screen::class.simpleName!!

                    key(id) {
                        TrackingEffect(id) {
                            commands
                                .filter { id == (it as? ScreenCmd)?.id }
                                .collect { cmd ->
                                    resolve(cmd, context, this)
                                }
                        }
                    }
                }

                TrackingEffect(Unit) {
                    commands
                        .filter { it !is ScreenCmd }
                        .collect { cmd ->
                            resolve(cmd, context, this)
                        }
                }
            }
        }
    )

    launch {
        component(flowOf("A", "B", "C", "R", "R", "R")).collect { println(it) }
    }
}

internal fun resolve(cmd: Cmd, @Suppress("UnusedParameter") sink: Sink<String>, scope: TrackingScope) {
    println("Resolving command: $cmd, ${scope.coroutineContext[CoroutineName.Key]}")

    scope.launch {
        try {
            delay(Long.MAX_VALUE)
        } finally {
            println("done $cmd, ${scope.coroutineContext[CoroutineName.Key]}")
        }
    }
}
