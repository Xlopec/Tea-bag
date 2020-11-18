@file:Suppress("FunctionName")

package com.max.reader.app

import com.max.reader.screens.article.list.ArticlesLoadingState
import com.max.reader.screens.article.list.LoadCriteria
import com.oliynick.max.tea.core.Initializer
import java.util.*

fun AppInitializer(): Initializer<AppState, Command> {

    val initScreen = ArticlesLoadingState(
        UUID.randomUUID(),
        LoadCriteria.Query("android")
    )

    return Initializer(
        AppState(initScreen),
        LoadByCriteria(
            initScreen.id,
            initScreen.criteria
        )
    )
}
