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

import com.oliynick.max.entities.shared.UUID
import com.oliynick.max.reader.app.command.Command
import com.oliynick.max.reader.app.navigation.NavigationStack
import com.oliynick.max.reader.app.navigation.push
import com.oliynick.max.reader.app.navigation.screen
import com.oliynick.max.tea.core.component.UpdateWith
import com.oliynick.max.tea.core.component.command
import com.oliynick.max.tea.core.component.noCommand
import kotlinx.collections.immutable.persistentListOf

typealias ScreenId = UUID

@ImmutableType
data class AppState(
    val isInDarkMode: Boolean,
    val screens: NavigationStack,
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

    val (updatedStack, commands) = if (id == null) {
        screens.updateAllScreens(how)
    } else {
        screens.updateScreen(id, how)
    }

    return copy(screens = updatedStack) command commands
}

@PublishedApi
internal inline fun <reified T : ScreenState> NavigationStack.updateAllScreens(
    noinline how: (T) -> UpdateWith<T, Command>,
): UpdateWith<NavigationStack, Command> {
    val builder = builder()
    val commands = foldIndexed(mutableSetOf<Command>()) { i, cmds, screen ->
        if (screen is T) {
            val (new, commands) = how(screen)

            builder[i] = new
            cmds += commands
        }
        cmds
    }

    return builder.build() to commands
}

@PublishedApi
internal inline fun <reified T : ScreenState> NavigationStack.updateScreen(
    id: ScreenId,
    noinline how: (T) -> UpdateWith<T, Command>,
): UpdateWith<NavigationStack, Command> {
    val screenIdx = indexOfFirst { it.id == id && it is T }
        .takeIf { it >= 0 } ?: return noCommand()

    val (updated, commands) = how(this[screenIdx] as T)

    return set(screenIdx, updated) to commands
}

fun AppState.pushScreen(
    screen: ScreenState,
): AppState = copy(screens = screens.push(screen))
