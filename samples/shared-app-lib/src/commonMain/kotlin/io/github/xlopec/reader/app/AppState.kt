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
import io.github.xlopec.reader.app.feature.navigation.AppNavigationStack
import io.github.xlopec.tea.core.Update
import io.github.xlopec.tea.core.command
import io.github.xlopec.tea.data.UUID
import io.github.xlopec.tea.navigation.mutate
import io.github.xlopec.tea.navigation.screen
import io.github.xlopec.tea.navigation.stackOf

public typealias ScreenId = UUID

@Immutable
public data class AppState internal constructor(
    val settings: Settings,
    val screens: AppNavigationStack,
) {

    internal constructor(
        screen: TabScreen,
        settings: Settings,
    ) : this(settings, stackOf(screen))
}

public inline val AppState.screen: Screen
    get() = screens.screen

public inline fun <reified T : Screen> AppState.updateScreen(
    id: ScreenId? = null,
    noinline how: (T) -> Update<T, Command>,
): Update<AppState, Command> {
    val (updatedStack, commands) = screens.mutate {
        if (id == null) {
            updateInstanceOf(how)
        } else {
            updateInstanceOfById(id, how)
        }
    }

    return copy(screens = updatedStack) command commands
}
