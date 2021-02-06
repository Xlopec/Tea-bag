/*
 * Copyright (C) 2021. Maksym Oliinyk.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:Suppress("FunctionName")

package com.max.reader.screens.article.list

import com.max.reader.app.env.HasAppContext
import com.max.reader.app.env.storage.HasGson
import com.max.reader.app.env.storage.Storage
import com.max.reader.screens.article.list.resolve.ArticlesResolver
import com.max.reader.screens.article.list.resolve.LiveArticlesResolver
import com.max.reader.screens.article.list.update.ArticlesUpdater
import com.max.reader.screens.article.list.update.LiveArticlesUpdater

interface ArticlesModule<Env> : ArticlesUpdater, ArticlesResolver<Env>

fun <Env> ArticlesModule(): ArticlesModule<Env> where Env : HasAppContext,
                                                      Env : HasGson,
                                                      Env : Storage<Env> =

    object : ArticlesModule<Env>,
        ArticlesUpdater by LiveArticlesUpdater,
        ArticlesResolver<Env> by LiveArticlesResolver() {
    }
