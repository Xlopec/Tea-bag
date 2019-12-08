package com.max.weatherviewer.app

import com.max.weatherviewer.Command
import com.oliynick.max.elm.core.component.UpdateWith
import com.oliynick.max.elm.core.component.command
import com.oliynick.max.elm.core.component.noCommand
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.immutableListOf

abstract class Screen

data class State(
    val screens: ImmutableList<Screen>
) {

    constructor(screen: Screen) : this(immutableListOf(screen))

    init {
        require(screens.isNotEmpty())
    }
}

inline val State.screen: Screen
    get() = screens.last()

inline fun <reified T : Screen> State.updateScreen(
    how: (T) -> UpdateWith<T, Command>
): UpdateWith<State, Command> {

    val index = screens.indexOfFirst { screen -> screen is T }

    if (index < 0) {
        return noCommand()
    }

    val (screen, commands) = how(screens[index] as T)

    return copy(screens = screens.set(index, screen)) command commands
}
