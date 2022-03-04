package com.oliynick.max.reader.app.feature.suggest

import com.oliynick.max.reader.app.ScreenId
import com.oliynick.max.reader.app.ScreenMessage
import kotlinx.collections.immutable.ImmutableList

sealed interface SuggestMessage : ScreenMessage {
    val id: ScreenId
}

data class SuggestionsLoaded(
    override val id: ScreenId,
    val suggestions: ImmutableList<String>,
) : SuggestMessage

data class SuggestionQueryUpdated(
    override val id: ScreenId,
    val query: String,
) : SuggestMessage
