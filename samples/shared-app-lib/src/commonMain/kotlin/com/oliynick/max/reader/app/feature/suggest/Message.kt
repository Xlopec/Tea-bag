package com.oliynick.max.reader.app.feature.suggest

import com.oliynick.max.reader.app.ScreenId
import com.oliynick.max.reader.app.ScreenMessage
import com.oliynick.max.reader.app.feature.network.Source
import kotlinx.collections.immutable.ImmutableList

sealed interface SuggestMessage : ScreenMessage {
    val id: ScreenId
}

data class SuggestionsLoaded(
    override val id: ScreenId,
    val suggestions: ImmutableList<String>,
    val sources: ImmutableList<Source>,
) : SuggestMessage

data class SuggestionQueryUpdated(
    override val id: ScreenId,
    val query: String,
) : SuggestMessage
