package com.oliynick.max.reader.app.feature.filter

import com.oliynick.max.reader.app.AppException
import com.oliynick.max.reader.app.ScreenId
import com.oliynick.max.reader.app.ScreenMessage
import com.oliynick.max.reader.app.domain.Query
import com.oliynick.max.reader.app.domain.Source
import com.oliynick.max.reader.app.domain.SourceId
import kotlinx.collections.immutable.ImmutableList
import kotlin.jvm.JvmInline

sealed interface FilterMessage : ScreenMessage {
    val id: ScreenId
}

data class InputChanged(
    override val id: ScreenId,
    val query: Query?
) : FilterMessage

@JvmInline
value class LoadSources(
    override val id: ScreenId,
) : FilterMessage

@JvmInline
value class ClearSelection(
    override val id: ScreenId,
) : FilterMessage

data class ToggleSourceSelection(
    override val id: ScreenId,
    val sourceId: SourceId
) : FilterMessage

data class SuggestionsLoaded(
    override val id: ScreenId,
    val suggestions: ImmutableList<Query>,
) : FilterMessage

data class SourcesLoadException(
    override val id: ScreenId,
    val exception: AppException
) : FilterMessage

data class SourcesLoaded(
    override val id: ScreenId,
    val sources: ImmutableList<Source>,
) : FilterMessage
