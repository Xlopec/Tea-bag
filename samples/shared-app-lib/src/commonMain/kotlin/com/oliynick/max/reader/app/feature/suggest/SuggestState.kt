package com.oliynick.max.reader.app.feature.suggest

import com.oliynick.max.entities.shared.UUID
import com.oliynick.max.reader.app.FullScreen
import com.oliynick.max.reader.app.ScreenId

class SuggestState(
    override val id: ScreenId = UUID()
) : FullScreen {
    val suggestions = listOf(
        "politics",
        "development",
        "design",
        "whatever"
    )
}