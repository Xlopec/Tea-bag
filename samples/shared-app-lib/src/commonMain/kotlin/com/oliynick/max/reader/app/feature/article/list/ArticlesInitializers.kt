@file:Suppress("FunctionName")

package com.oliynick.max.reader.app.feature.article.list

import com.oliynick.max.reader.app.ScreenId
import com.oliynick.max.reader.app.feature.article.list.Paging.Companion.FirstPage
import com.oliynick.max.tea.core.component.command

internal fun ArticlesInitialUpdate(
    id: ScreenId,
    type: FilterType
) = ArticlesState.newLoading(id, Filter(type)) command DoLoadFilter(id, type)

fun ArticlesInitialUpdate(
    id: ScreenId,
    filter: Filter
) = ArticlesState.newLoading(id, filter) command DoLoadArticles(id, filter, FirstPage)
