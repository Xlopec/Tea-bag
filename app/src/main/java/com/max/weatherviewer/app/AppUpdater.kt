package com.max.weatherviewer.app

import com.max.weatherviewer.CloseApp
import com.max.weatherviewer.Command
import com.max.weatherviewer.home.Home
import com.max.weatherviewer.home.HomeMessage
import com.max.weatherviewer.home.HomeUpdater
import com.oliynick.max.elm.core.component.UpdateWith
import com.oliynick.max.elm.core.component.command
import com.oliynick.max.elm.core.component.noCommand
import kotlinx.collections.immutable.ImmutableList

object AppUpdater {

    fun update(message: Message, state: State): UpdateWith<State, Command> {
        return when (message) {
            is Navigation -> navigate(message, state)
            is ScreenMessage -> update(message, state)
        }
    }

    fun update(message: ScreenMessage, state: State) : UpdateWith<State, Command> {
        return when(message) {
            is HomeMessage -> state.updateScreen<Home> { home ->
                HomeUpdater.update(
                    message,
                    home
                )
            }
            else -> TODO()
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

private fun <T> ImmutableList<T>.pop() = if (lastIndex >= 0) removeAt(lastIndex) else this