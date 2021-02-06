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

package com.max.reader.screens.article.details

import com.max.reader.app.env.HasAppContext
import com.max.reader.app.resolve.HasCommandTransport
import com.max.reader.screens.article.details.resolve.ArticleDetailsResolver
import com.max.reader.screens.article.details.resolve.LiveArticleDetailsResolver
import com.max.reader.screens.article.details.update.ArticleDetailsUpdater
import com.max.reader.screens.article.details.update.LiveArticleDetailsUpdater
import com.max.reader.screens.article.list.resolve.ArticlesResolver

interface ArticleDetailsModule<Env> : ArticleDetailsUpdater, ArticleDetailsResolver<Env>

fun <Env> ArticleDetailsModule(): ArticleDetailsModule<Env> where Env : HasCommandTransport,
                                                                  Env : ArticlesResolver<Env>,
                                                                  Env : HasAppContext,
                                                                  Env : ArticleDetailsResolver<Env> =
    object : ArticleDetailsModule<Env>,
        ArticleDetailsUpdater by LiveArticleDetailsUpdater,
        ArticleDetailsResolver<Env> by LiveArticleDetailsResolver() {
    }
