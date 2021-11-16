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
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList

typealias ScreenId = UUID

data class AppState(
    val isInDarkMode: Boolean,
    /**
     * Holds application stack of screens
     * invariants: [Screen 0..*, TabScreen 1..3]
     */
    val screens: PersistentList<ScreenState>,
) {

    constructor(
        screen: TabScreen,
        isAppInDarkMode: Boolean
    ) : this(isAppInDarkMode, persistentListOf(screen))

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

    val newScreens = ArrayList<ScreenState>(screens.size)
    val resultCommands = mutableSetOf<Command>()

    for (i in screens.indices) {
        // todo refactor
        val current = screens[i]

        if (id == null) {

            if (current is T) {
                val (new, commands) = how(current)

                newScreens.add(new)
                resultCommands += commands
            } else {
                newScreens.add(current)
            }

        } else if (current.id == id && current is T) {
            val (screen, commands) = how(current)

            resultCommands += commands
            newScreens.add(screen)

        } else {
            newScreens.add(current)
        }
    }

    return copy(screens = newScreens.toPersistentList()) command resultCommands
}

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

expect fun AppState.popScreen(): UpdateWith<AppState, Command>
