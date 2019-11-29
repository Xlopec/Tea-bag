package com.max.weatherviewer.app

import com.max.weatherviewer.CloseApp
import com.max.weatherviewer.Command
import com.max.weatherviewer.HomeCommand
import com.max.weatherviewer.home.HomeDependencies
import com.max.weatherviewer.home.HomeResolver
import com.oliynick.max.elm.core.component.UpdateWith
import com.oliynick.max.elm.core.component.command
import com.oliynick.max.elm.core.component.noCommand
import com.oliynick.max.elm.core.component.sideEffect
import kotlinx.coroutines.channels.Channel
import retrofit2.Retrofit

data class AppDependencies(
    val isDebugBuild: Boolean,
    val closeAppCommands: Channel<CloseApp>,
    val retrofit: Retrofit,
    val homeDependencies: HomeDependencies
)

object AppResolver {

    suspend fun resolve(dependencies: AppDependencies, command: Command): Set<Message> {

        suspend fun resolve(command: Command): Set<Message> =
            dependencies.run {
                when (command) {
                    CloseApp -> command.sideEffect { dependencies.closeAppCommands.offer(command as CloseApp) }
                    is HomeCommand -> HomeResolver.resolve(
                        dependencies.homeDependencies,
                        command
                    )
                }
            }

        // todo error handling
        return runCatching { resolve(command) }
            .getOrThrow()//Else { emptySet() }
    }

}

inline fun <reified T : Screen> State.updateScreen(how: (T) -> UpdateWith<T, Command>): UpdateWith<State, Command> {
    val index = screens.indexOfFirst { screen -> screen is T }

    if (index < 0) {
        return noCommand()
    }

    val (screen, commands) = how(screens[index] as T)

    return copy(screens = screens.set(index, screen)) command commands
}