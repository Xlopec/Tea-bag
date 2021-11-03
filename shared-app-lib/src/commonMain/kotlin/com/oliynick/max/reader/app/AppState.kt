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

interface TabScreen : ScreenState {
    // things for consideration:
    // 1 how to make fast search & update for nested screens
    // 2 how to keep class layout as simple as possible with p1 in mind
    // 3 how to avoid code duplication? (consider Arrow Meta optics API)
    // fixme this probably should go as extensions
    // probably I can make typealias for this and implement all the necessary operations on the top of it
    val screens: PersistentList<ScreenState>
    fun pop(): TabScreen
    fun contains(id: ScreenId): Boolean
    fun <T : ScreenState> update(
        id: ScreenId,
        how: (T) -> UpdateWith<T, Command>
    ): UpdateWith<TabScreen, Command>
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
    get() = screens[0]

inline fun <reified T : ScreenState> AppState.updateScreen(
    id: ScreenId?,
    noinline how: (T) -> UpdateWith<T, Command>,
): UpdateWith<AppState, Command> {


    for (i in screens.indices) {

        val current = screens[i]

        if (current.id == id || id == null /*fixme shit*/) {
            val (screen, commands) = how(current as T)

            return copy(screens = screens.set(i, screen)) command commands

        } else if (current is TabScreen && current.contains(id)) {
            val (screen, commands) = current.update(id, how)

            return copy(screens = screens.set(i, screen)) command commands
        }
    }

    return noCommand()
}

fun AppState.dropTopScreen() =
    copy(screens = screens.pop())

// new api
inline fun <T : ScreenState> AppState.updateTopScreen(
    how: () -> T
) = copy(screens = screens.set(0, how()))

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
): AppState = copy(screens = screens.add(0, screen))

// todo refactor this bit
fun AppState.popScreen(): AppState {
    val screen = screen

    return if (screen is TabScreen) {
        if (screen.screens.isEmpty()) {
            this // should close app
        } else {
            updateTopScreen { screen.pop() }
        }
    } else {
        dropTopScreen()
    }
}

fun <T> PersistentList<T>.pop() = if (isEmpty()) this else removeAt(0)
