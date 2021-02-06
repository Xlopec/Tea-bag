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

package com.max.reader.app

import com.max.reader.screens.article.list.ArticlesState
import com.max.reader.screens.article.list.Query
import com.max.reader.screens.article.list.QueryType
import com.oliynick.max.tea.core.Initializer
import java.util.*

fun AppInitializer(
    isDarkModeEnabled: Boolean,
): Initializer<AppState, Command> {

    val initScreen = ArticlesState.newLoading(
        UUID.randomUUID(),
        Query("android", QueryType.Regular),
    )

    return Initializer(
        AppState(initScreen, isDarkModeEnabled),
        LoadArticlesByQuery(
            initScreen.id,
            initScreen.query,
            initScreen.articles.size,
            ArticlesState.ArticlesPerPage
        )
    )
}
