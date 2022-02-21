package com.oliynick.max.reader.app.feature.suggest

import com.oliynick.max.reader.app.ScreenId
import com.oliynick.max.reader.app.ScreenMessage

sealed interface SuggestMessage : ScreenMessage {
    val id: ScreenId
}

data class SuggestionQueryUpdated(
    override val id: ScreenId,
    val query: String,
) : SuggestMessage
