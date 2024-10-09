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
import io.github.xlopec.reader.app.command.DoLog
import io.github.xlopec.reader.app.command.DoStoreDarkMode
import io.github.xlopec.reader.app.feature.article.details.ArticleDetailsMessage
import io.github.xlopec.reader.app.feature.article.details.ArticleDetailsState
import io.github.xlopec.reader.app.feature.article.details.toArticleDetailsUpdate
import io.github.xlopec.reader.app.feature.article.list.ArticlesMessage
import io.github.xlopec.reader.app.feature.article.list.ArticlesState
import io.github.xlopec.reader.app.feature.article.list.toArticlesUpdate
import io.github.xlopec.reader.app.feature.article.list.updateArticles
import io.github.xlopec.reader.app.feature.filter.FilterMessage
import io.github.xlopec.reader.app.feature.filter.FiltersState
import io.github.xlopec.reader.app.feature.filter.toFiltersUpdate
import io.github.xlopec.reader.app.feature.filter.updateFilters
import io.github.xlopec.reader.app.feature.navigation.Navigation
import io.github.xlopec.reader.app.feature.navigation.navigate
import io.github.xlopec.reader.app.feature.settings.SettingsMessage
import io.github.xlopec.reader.app.feature.settings.SystemDarkModeChanged
import io.github.xlopec.reader.app.feature.settings.ToggleDarkMode
import io.github.xlopec.tea.core.Update
import io.github.xlopec.tea.core.command
import io.github.xlopec.tea.core.noCommand

public fun <Env> AppUpdater(): AppUpdater<Env> =
    AppUpdater { message, state ->
        when (message) {
            is Navigation -> navigate(message, state)
            is ScreenMessage -> updateScreen(message, state)
            else -> error("can't get here, $message")
        }
    }

private fun updateScreen(
    message: ScreenMessage,
    state: AppState,
): Update<AppState, Command> =
    when (message) {
        is FilterMessage -> state.updateScreen<FiltersState>(message.id) { screen ->
            screen.toFiltersUpdate(message)
        }
        is ArticlesMessage -> state.updateScreen<ArticlesState>(message.id) { screen ->
            screen.toArticlesUpdate(message)
        }
        is ArticleDetailsMessage -> state.updateScreen<ArticleDetailsState>(message.id) { screen ->
            screen.toArticleDetailsUpdate(message)
        }
        is SettingsMessage -> state.toSettingsUpdate(message)
        is FilterUpdated -> state.updateScreen<Screen> { screen ->
            screen.toFiltersBroadcastUpdate(message)
        }
        is Log -> state.toLogUpdate(message)
        else -> error("Unknown screen message, was $message")
    }

private fun Screen.toFiltersBroadcastUpdate(
    message: FilterUpdated
) = when (this) {
    is ArticlesState -> updateArticles(message, this)
    is FiltersState -> updateFilters(message, this)
    else -> noCommand()
}

private fun AppState.toLogUpdate(
    message: Log
) = command(DoLog(this, message.throwable, message.id, message.causedBy))

private fun AppState.toSettingsUpdate(
    message: SettingsMessage,
): Update<AppState, Command> =
    when (message) {
        is ToggleDarkMode -> updateSettings {
            updated(
                userDarkModeEnabled = message.userDarkModeEnabled,
                syncWithSystemDarkModeEnabled = message.syncWithSystemDarkModeEnabled
            )
        } command DoStoreDarkMode(message.userDarkModeEnabled, message.syncWithSystemDarkModeEnabled)
        is SystemDarkModeChanged -> updateSettings { updated(systemDarkModeEnabled = message.enabled) }.noCommand()
    }

private fun AppState.updateSettings(
    how: Settings.() -> Settings
) = copy(settings = settings.run(how))
