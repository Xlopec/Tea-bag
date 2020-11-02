@file:Suppress("FunctionName")

package com.max.reader.app

import com.max.reader.screens.feed.FeedLoading
import com.max.reader.screens.feed.LoadCriteria
import com.oliynick.max.tea.core.Initializer
import java.util.*

fun AppInitializer(): Initializer<State, Command> {

    val initScreen = FeedLoading(
        UUID.randomUUID(),
        LoadCriteria.Query("android")
    )

    return Initializer(
        State(initScreen),
        LoadByCriteria(
            initScreen.id,
            initScreen.criteria
        )
    )
}
