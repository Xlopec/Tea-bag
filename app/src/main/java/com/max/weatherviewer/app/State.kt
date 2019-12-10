package com.max.weatherviewer.app

import com.max.weatherviewer.Command
import com.oliynick.max.elm.core.component.UpdateWith
import com.oliynick.max.elm.core.component.command
import com.oliynick.max.elm.core.component.noCommand
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.immutableListOf
import java.util.*

typealias ScreenId = UUID

abstract class Screen {
    abstract val id: ScreenId
}

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
    id: ScreenId,
    how: (T) -> UpdateWith<T, Command>
): UpdateWith<State, Command> {

    val index = screens.indexOfFirst { screen -> screen.id == id && screen is T }

    if (index < 0) {
        return noCommand()
    }

    val (screen, commands) = how(screens[index] as T)

    return copy(screens = screens.set(index, screen)) command commands
}

fun State.swapScreens(
    i: Int,
    j: Int = screens.lastIndex
): State {

    if (i == j) return this

    val tmp = screens[j]

    return copy(screens = screens.set(j, screens[i]).set(i, tmp))
}

fun State.pushScreen(
    screen: Screen
): State = copy(screens = screens.add(screen))

fun State.popScreen(
): State = copy(screens = screens.pop())

private fun <T> ImmutableList<T>.pop() = if (lastIndex >= 0) removeAt(lastIndex) else this
