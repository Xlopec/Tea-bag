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

@file:Suppress("FunctionName")

package io.github.xlopec.reader.app

import io.github.xlopec.reader.app.command.Command
import io.github.xlopec.reader.app.feature.article.list.ArticlesInitialUpdate
import io.github.xlopec.reader.app.feature.navigation.NavigateToFeed
import io.github.xlopec.reader.app.model.FilterType.Regular
import io.github.xlopec.tea.core.Initial
import io.github.xlopec.tea.core.Initializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

public fun AppInitializer(
    systemDarkModeEnabled: Boolean,
    environment: Environment
): Initializer<AppState, Command> = Initializer(Dispatchers.IO) {

    val filter = environment.findFilter(Regular)
    val (screen, commands) = ArticlesInitialUpdate(NavigateToFeed.tab, filter)

    val settings = Settings(
        syncWithSystemDarkModeEnabled = environment.isSyncWithSystemDarkModeEnabled(),
        systemDarkModeEnabled = systemDarkModeEnabled,
        userDarkModeEnabled = environment.isDarkModeEnabled()
    )

    Initial(AppState(screen, settings), commands)
}
