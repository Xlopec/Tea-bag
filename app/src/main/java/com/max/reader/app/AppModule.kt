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

package com.max.reader.app

import com.max.reader.app.env.storage.local.LocalStorage
import com.max.reader.app.resolve.AppResolver
import com.max.reader.app.update.AppNavigation
import com.max.reader.app.update.AppUpdater
import com.max.reader.screens.article.details.resolve.ArticleDetailsResolver
import com.max.reader.screens.article.details.update.ArticleDetailsUpdater
import com.max.reader.screens.article.list.resolve.ArticlesResolver
import com.max.reader.screens.article.list.update.ArticlesUpdater
import kotlinx.coroutines.flow.MutableSharedFlow

interface AppModule<Env> : AppUpdater<Env>, AppResolver<Env>, AppNavigation

fun <Env> AppModule(
    closeCommands: MutableSharedFlow<CloseApp>,
): AppModule<Env> where Env : ArticlesResolver<Env>,
                        Env : ArticleDetailsResolver<Env>,
                        Env : ArticlesUpdater,
                        Env : LocalStorage,
                        Env : AppNavigation,
                        Env : ArticleDetailsUpdater =
    object : AppModule<Env>,
        AppNavigation by AppNavigation(),
        AppUpdater<Env> by AppUpdater(),
        AppResolver<Env> by AppResolver(closeCommands) {

    }
