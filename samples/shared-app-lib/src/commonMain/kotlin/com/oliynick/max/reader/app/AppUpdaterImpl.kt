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
import com.oliynick.max.reader.app.command.DoStoreDarkMode
import com.oliynick.max.reader.app.feature.article.details.ArticleDetailsMessage
import com.oliynick.max.reader.app.feature.article.details.ArticleDetailsState
import com.oliynick.max.reader.app.feature.article.details.ArticleDetailsUpdater
import com.oliynick.max.reader.app.feature.article.list.ArticlesMessage
import com.oliynick.max.reader.app.feature.article.list.ArticlesState
import com.oliynick.max.reader.app.feature.article.list.ArticlesUpdater
import com.oliynick.max.reader.app.feature.navigation.NavigationUpdater
import com.oliynick.max.reader.app.feature.settings.SettingsMessage
import com.oliynick.max.reader.app.feature.settings.ToggleDarkMode
import com.oliynick.max.tea.core.component.UpdateWith
import com.oliynick.max.tea.core.component.command

fun <Env> AppUpdater(): AppUpdater<Env> where Env : ArticlesUpdater,
                                              Env : ArticleDetailsUpdater,
                                              Env : NavigationUpdater =
    AppUpdater { message, state ->
        when (message) {
            is Navigation -> navigate(message, state)
            is ScreenMessage -> updateScreen(message, state)
        }
    }

fun <Env> Env.updateScreen(
    message: ScreenMessage,
    state: AppState,
): UpdateWith<AppState, Command> where Env : ArticlesUpdater,
                                       Env : ArticleDetailsUpdater,
                                       Env : NavigationUpdater =
    when (message) {
        is ArticlesMessage -> state.updateScreen<ArticlesState>(message.id) { screen ->
            updateArticles(message, screen)
        }
        is ArticleDetailsMessage -> state.updateScreen<ArticleDetailsState>(message.id) { screen ->
            updateArticleDetails(message, screen)
        }
        is SettingsMessage -> state.updateSettings(message)
        else -> error("Unknown screen message, was $message")
    }

fun AppState.updateSettings(
    message: SettingsMessage,
): UpdateWith<AppState, Command> =
    when (message) {
        is ToggleDarkMode -> copy(isInDarkMode = message.enable) command DoStoreDarkMode(message.enable)
    }
