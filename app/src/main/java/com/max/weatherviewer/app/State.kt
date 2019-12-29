package com.max.weatherviewer.app

import com.oliynick.max.elm.core.component.UpdateWith
import com.oliynick.max.elm.core.component.command
import com.oliynick.max.elm.core.component.noCommand
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import java.util.*
import kotlin.collections.ArrayList

typealias ScreenId = UUID

abstract class Screen {
    abstract val id: ScreenId
}

data class State(
  //  @JsonSerialize(using = PersistentListSerializer::class)
    val screens: PersistentList<Screen>
) {

    constructor(screen: Screen) : this(persistentListOf(screen))

    init {
        require(screens.isNotEmpty())
    }
}

inline val State.screen: Screen
    get() = screens.last()

inline fun <reified T : Screen> State.updateScreen(
    id: ScreenId?,
    how: (T) -> UpdateWith<T, Command>
): UpdateWith<State, Command> {

    if (id == null) {
        return updateScreen(how)
    }

    val index = screens.indexOfFirst { screen -> screen.id == id && screen is T }

    if (index < 0) {
        return noCommand()
    }

    val (screen, commands) = how(screens[index] as T)

    return copy(screens = screens.set(index, screen)) command commands
}

inline fun <reified T : Screen> State.updateScreen(
    how: (T) -> UpdateWith<T, Command>
): UpdateWith<State, Command> {

    val cmds = mutableSetOf<Command>()
    val scrs = screens.fold(ArrayList<Screen>(screens.size)) { acc, screen ->

        if (screen is T) {
            val (updatedScreen, commands) = how(screen)

            cmds += commands
            acc += updatedScreen
        } else {
            acc += screen
        }

        acc
    }.toPersistentList()

    return copy(screens = scrs) command cmds
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

fun State.popScreen(): State = copy(screens = screens.pop())

private fun <T> PersistentList<T>.pop() = if (lastIndex >= 0) removeAt(lastIndex) else this
