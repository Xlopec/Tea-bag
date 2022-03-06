package com.oliynick.max.reader.app.feature.suggest

import com.oliynick.max.reader.app.ScreenId
import com.oliynick.max.reader.app.ScreenMessage
import com.oliynick.max.reader.app.feature.network.Source
import kotlinx.collections.immutable.ImmutableList
import kotlin.jvm.JvmInline

sealed interface SuggestMessage : ScreenMessage {
    val id: ScreenId
}

@JvmInline
value class LoadSources(
    override val id: ScreenId,
) : SuggestMessage

@JvmInline
value class ClearSelection(
    override val id: ScreenId,
) : SuggestMessage

data class ToggleSourceSelection(
    override val id: ScreenId,
    val source: Source
) : SuggestMessage

data class SuggestionsLoaded(
    override val id: ScreenId,
    val suggestions: ImmutableList<String>,
) : SuggestMessage

data class SourcesLoaded(
    override val id: ScreenId,
    val sources: ImmutableList<Source>,
) : SuggestMessage

data class SuggestionQueryUpdated(
    override val id: ScreenId,
    val query: String,
) : SuggestMessage
