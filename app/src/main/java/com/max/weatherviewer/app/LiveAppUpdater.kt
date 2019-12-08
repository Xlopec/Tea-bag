package com.max.weatherviewer.app

import com.max.weatherviewer.CloseApp
import com.max.weatherviewer.Command
import com.max.weatherviewer.home.Home
import com.max.weatherviewer.home.HomeMessage
import com.max.weatherviewer.home.HomeUpdater
import com.max.weatherviewer.home.LiveHomeUpdater.update
import com.oliynick.max.elm.core.component.UpdateWith
import com.oliynick.max.elm.core.component.command
import com.oliynick.max.elm.core.component.noCommand
import kotlinx.collections.immutable.ImmutableList

interface AppUpdater<Env> {

    fun Env.update(
        message: Message,
        state: State
    ): UpdateWith<State, Command>

}

fun <Env> AppUpdater(): AppUpdater<Env> where Env : HomeUpdater = object : LiveAppUpdater<Env> {}

interface LiveAppUpdater<Env> : AppUpdater<Env> where Env : HomeUpdater {

    override fun Env.update(
        message: Message,
        state: State
    ): UpdateWith<State, Command> =
        when (message) {
            is Navigation -> navigate(message, state)
            is ScreenMessage -> updateScreen(message, state)
        }

    fun updateScreen(
        message: ScreenMessage,
        state: State
    ): UpdateWith<State, Command> =
        when (message) {
            is HomeMessage -> state.updateScreen<Home> { home -> update(message, home) }
            else -> error("Unhandled screen message $message for state $state")
        }

    fun navigate(
        nav: Navigation,
        state: State
    ): UpdateWith<State, Command> =
        state.run {
            when {
                nav is NavigateTo -> copy(screens = screens.add(nav.screen)).noCommand()
                nav === Pop && screens.size > 1 -> copy(screens = screens.pop()).noCommand()
                nav === Pop && screens.size == 1 -> state command CloseApp
                else -> error("Unexpected state")
            }
        }

}

private fun <T> ImmutableList<T>.pop() = if (lastIndex >= 0) removeAt(lastIndex) else this
