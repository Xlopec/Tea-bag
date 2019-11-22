package com.max.weatherviewer

import com.oliynick.max.elm.core.actor.component
import com.oliynick.max.elm.core.component.*
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel

fun CoroutineScope.appComponent(dependencies: Dependencies): Component<Message, State> {

    suspend fun resolver(command: Command) = AppResolver.resolve(dependencies, command)

    return component(State(), ::resolver, AppReducer::update, DoLoadArticles("bitcoin")) {

    }
}

data class Dependencies(
    val closeAppCommands: Channel<CloseApp>,
    val newsApi: NewsApi
)

object AppReducer {

    fun update(message: Message, state: State): UpdateWith<State, Command> {
        return when (message) {
            is Navigation -> navigate(message, state)
            is HomeMessage -> HomeReducer.update(message, state)
        }
    }

    private fun navigate(nav: Navigation, state: State): UpdateWith<State, Command> {
        return state.run {
            when {
                nav is NavigateTo -> copy(screens = screens.add(nav.screen)).noCommand()
                nav === Pop && screens.size > 1 -> copy(screens = screens.pop()).noCommand()
                nav === Pop && screens.size == 1 -> state command CloseApp
                else -> error("Unexpected state")
            }
        }
    }

}

object AppResolver {

    suspend fun resolve(dependencies: Dependencies, command: Command): Set<Message> {

        suspend fun resolve(command: Command): Set<Message> =
            dependencies.run {
                when (command) {
                    CloseApp -> command.sideEffect { dependencies.closeAppCommands.offer(command as CloseApp) }
                    is DoLoadArticles -> command.effect { ArticlesLoaded(newsApi(command.query)) }
                }
            }

        // todo error handling
        return runCatching { resolve(command) }
            .getOrThrow()//Else { emptySet() }
    }

}

private fun <T> ImmutableList<T>.pop() = if (lastIndex >= 0) removeAt(lastIndex) else this