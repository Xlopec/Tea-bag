package com.oliynick.max.reader.app.feature.suggest

import com.oliynick.max.reader.app.FullScreen
import com.oliynick.max.reader.app.ScreenId
import com.oliynick.max.reader.app.feature.article.list.Query

data class SuggestState(
    override val id: ScreenId,
    val query: Query,
) : FullScreen {
    val suggestions = listOf(
        "politics",
        "development",
        "design",
        "whatever"
    )
}