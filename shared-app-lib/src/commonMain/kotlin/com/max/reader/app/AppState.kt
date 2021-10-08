/*
 * MIT License
 *
 * Copyright (c) 2021. Maksym Oliinyk.
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

package com.max.reader.app

import com.oliynick.max.reader.app.UUID
import com.oliynick.max.tea.core.component.UpdateWith
import com.oliynick.max.tea.core.component.command
import com.oliynick.max.tea.core.component.noCommand
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList

typealias ScreenId = UUID

interface ScreenState {
    val id: ScreenId
}

data class AppState(
    val isInDarkMode: Boolean,
    val screens: PersistentList<ScreenState>,
) {

    constructor(
        screen: ScreenState,
        isInDarkMode: Boolean
    ) : this(isInDarkMode, persistentListOf(screen))

    init {
        require(screens.isNotEmpty())
    }
}

inline val AppState.screen: ScreenState
    get() = screens.last()

inline fun <reified T : ScreenState> AppState.updateScreen(
    id: ScreenId?,
    how: (T) -> UpdateWith<T, Command>,
): UpdateWith<AppState, Command> {

    val index by lazy { screens.indexOfFirst { screen -> screen.id == id && screen is T } }

    return when {
        id == null -> updateScreen(how)
        index < 0 -> noCommand()
        else -> {
            val (screen, commands) = how(screens[index] as T)

            copy(screens = screens.set(index, screen)) command commands
        }
    }
}

inline fun <reified T : ScreenState> AppState.updateScreen(
    how: (T) -> UpdateWith<T, Command>,
): UpdateWith<AppState, Command> {

    val cmds = mutableSetOf<Command>()
    val scrs = screens.fold(ArrayList<ScreenState>(screens.size)) { acc, screen ->

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

fun AppState.swapWithLast(
    i: Int,
) = swapScreens(i, screens.lastIndex)

fun AppState.swapScreens(
    i: Int,
    j: Int,
): AppState {

    if (i == j) return this

    val tmp = screens[j]

    return copy(screens = screens.set(j, screens[i]).set(i, tmp))
}

fun AppState.pushScreen(
    screen: ScreenState,
): AppState = copy(screens = screens.add(screen))

fun AppState.popScreen(): AppState = copy(screens = screens.pop())

private fun <T> PersistentList<T>.pop() = if (lastIndex >= 0) removeAt(lastIndex) else this
