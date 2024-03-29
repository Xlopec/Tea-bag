/*
 * MIT License
 *
 * Copyright (c) 2022. Maksym Oliinyk.
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

package io.github.xlopec.reader.app

import androidx.compose.runtime.Immutable
import io.github.xlopec.reader.app.command.Command
import io.github.xlopec.reader.app.feature.navigation.*
import io.github.xlopec.tea.core.Update
import io.github.xlopec.tea.core.command
import io.github.xlopec.tea.data.UUID
import kotlinx.collections.immutable.persistentListOf

typealias ScreenId = UUID

@Immutable
data class AppState(
    val settings: Settings,
    val screens: NavigationStack,
) {

    constructor(
        screen: TabScreen,
        settings: Settings,
    ) : this(settings, persistentListOf(screen))

    init {
        require(screens.isNotEmpty())
    }
}

inline val AppState.screen: ScreenState
    get() = screens.screen

inline fun <reified T : ScreenState> AppState.updateScreen(
    id: ScreenId? = null,
    noinline how: (T) -> Update<T, Command>,
): Update<AppState, Command> {

    val (updatedStack, commands) = if (id == null) {
        screens.updateAllScreens(how)
    } else {
        screens.updateScreen(id, how)
    }

    return copy(screens = updatedStack) command commands
}

fun AppState.pushScreen(
    screen: ScreenState,
): AppState = copy(screens = screens.push(screen))
