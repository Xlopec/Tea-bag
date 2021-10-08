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

package com.max.reader.app.resolve

import com.oliynick.max.reader.app.ArticleDetailsCommand
import com.oliynick.max.reader.app.ArticlesCommand
import com.oliynick.max.reader.app.CloseApp
import com.oliynick.max.reader.app.StoreDarkMode
import com.oliynick.max.reader.app.LocalStorage
import com.max.reader.screens.article.details.resolve.ArticleDetailsResolver
import com.max.reader.screens.article.list.resolve.ArticlesResolver
import com.oliynick.max.tea.core.component.sideEffect
import kotlinx.coroutines.flow.MutableSharedFlow

fun <Env> AppResolver(
    closeCommands: MutableSharedFlow<CloseApp>,
): AppResolver<Env> where Env : ArticlesResolver<Env>,
                          Env : LocalStorage,
                          Env : ArticleDetailsResolver<Env> =
    AppResolver { command ->
        when (command) {
            is CloseApp -> command.sideEffect { closeCommands.emit(command) }
            is ArticlesCommand -> resolve(command)
            is ArticleDetailsCommand -> resolve(command)
            is StoreDarkMode -> command.sideEffect { storeIsDarkModeEnabled(command.isEnabled) }
        }
    }
