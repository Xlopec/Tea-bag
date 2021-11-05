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

package com.oliynick.max.reader.app

import com.oliynick.max.tea.core.component.UpdateWith
import com.oliynick.max.tea.core.component.command
import com.oliynick.max.tea.core.component.noCommand
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

typealias ScreenId = UUID

interface ScreenState {
    val id: ScreenId
}

data class AppState(
    val isInDarkMode: Boolean,
    /**
     * Holds application stack of screens
     * invariants: [Screen 0..*, TabScreen 1..3]
     */
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
    get() = screens.screen

inline fun <reified T : ScreenState> AppState.updateScreen(
    id: ScreenId?,
    noinline how: (T) -> UpdateWith<T, Command>,
): UpdateWith<AppState, Command> {


    for (i in screens.indices) {

        val current = screens[i]

        if (current.id == id || id == null /*fixme shit*/) {
            val (screen, commands) = how(current as T)

            return copy(screens = screens.set(i, screen)) command commands

        } else if (current is TabScreen && id in current.screens) {
            val (screen, commands) = current.update(id, how)

            return copy(screens = screens.set(i, screen)) command commands
        }
    }

    return noCommand()
}

fun AppState.dropTopScreen() =
    copy(screens = screens.pop())

// new api
inline fun AppState.updateTopScreen(
    how: () -> ScreenState
) = copy(screens = screens.set(0, how()))

fun AppState.swapScreens(
    i: Int,
    j: Int,
): AppState {

    if (i == j) return this

    return copy(screens = screens.swap(i, j))
}

fun AppState.pushScreen(
    screen: ScreenState,
): AppState = copy(screens = screens.push(screen))

// todo refactor this bit
fun AppState.popScreen(): UpdateWith<AppState, CloseApp> {
    val screen = screen

    return if (screen is TabScreen) {
        if (screen.screens.isEmpty()) {
            this command CloseApp
        } else {
            updateTopScreen { screen.pop() }.noCommand()
        }
    } else {
        dropTopScreen().noCommand()
    }
}

