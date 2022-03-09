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

@file:Suppress("FunctionName")

package com.oliynick.max.reader.app

import com.oliynick.max.reader.app.command.Command
import com.oliynick.max.reader.app.command.DoLog
import com.oliynick.max.reader.app.command.DoStoreDarkMode
import com.oliynick.max.reader.app.feature.article.details.ArticleDetailsMessage
import com.oliynick.max.reader.app.feature.article.details.ArticleDetailsState
import com.oliynick.max.reader.app.feature.article.details.updateArticleDetails
import com.oliynick.max.reader.app.feature.article.list.ArticlesMessage
import com.oliynick.max.reader.app.feature.article.list.ArticlesState
import com.oliynick.max.reader.app.feature.article.list.updateArticles
import com.oliynick.max.reader.app.feature.navigation.Navigation
import com.oliynick.max.reader.app.feature.navigation.navigate
import com.oliynick.max.reader.app.feature.settings.SettingsMessage
import com.oliynick.max.reader.app.feature.settings.SystemDarkModeChanged
import com.oliynick.max.reader.app.feature.settings.ToggleDarkMode
import com.oliynick.max.reader.app.feature.suggest.SuggestMessage
import com.oliynick.max.reader.app.feature.suggest.SuggestState
import com.oliynick.max.reader.app.feature.suggest.updateSuggestions
import com.oliynick.max.tea.core.component.UpdateWith
import com.oliynick.max.tea.core.component.command
import com.oliynick.max.tea.core.component.noCommand

fun <Env> AppUpdater(): AppUpdater<Env> =
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
): UpdateWith<AppState, Command> =
    when (message) {
        is SuggestMessage -> state.updateScreen<SuggestState>(message.id) { screen ->
            updateSuggestions(message, screen)
        }
        is ArticlesMessage -> state.updateScreen<ArticlesState>(message.id) { screen ->
            updateArticles(message, screen)
        }
        is ArticleDetailsMessage -> state.updateScreen<ArticleDetailsState>(message.id) { screen ->
            updateArticleDetails(message, screen)
        }
        is SettingsMessage -> state.toSettingsUpdate(message)
        is Log -> state.toLogUpdate(message)
        else -> error("Unknown screen message, was $message")
    }

private fun AppState.toLogUpdate(
    message: Log
) = command(DoLog(this, message.throwable, message.id, message.causedBy))

private fun AppState.toSettingsUpdate(
    message: SettingsMessage,
): UpdateWith<AppState, Command> =
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
