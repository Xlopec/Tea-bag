@file:Suppress("FunctionName")

package com.max.reader.app

import com.max.reader.screens.article.list.ArticlesLoadingState
import com.max.reader.screens.article.list.Query
import com.max.reader.screens.article.list.QueryType
import com.oliynick.max.tea.core.Initializer
import java.util.*

fun AppInitializer(): Initializer<AppState, Command> {

    val initScreen = ArticlesLoadingState(
        UUID.randomUUID(),
        Query("android", QueryType.Regular)
    )

    return Initializer(
        AppState(initScreen),
        LoadByCriteria(
            initScreen.id,
            initScreen.query
        )
    )
}
