package com.oliynick.max.reader.app.feature.suggest

import com.oliynick.max.reader.app.FullScreen
import com.oliynick.max.reader.app.ScreenId
import com.oliynick.max.reader.app.feature.article.list.Filter
import com.oliynick.max.reader.app.feature.network.Source
import com.oliynick.max.reader.app.feature.network.SourceId
import com.oliynick.max.reader.app.misc.Loadable
import kotlinx.collections.immutable.*

data class TextFieldState(
    val filter: Filter,
    val cursorPosition: Int
)

data class SuggestState(
    override val id: ScreenId,
    val textFieldState: TextFieldState,
    val sources: Loadable<PersistentList<Source>>,
    val suggestions: ImmutableList<String> = persistentListOf(),
    val selectedSources: PersistentSet<SourceId> = persistentSetOf(),
) : FullScreen

fun SuggestState.isSelected(
    id: SourceId
) = id in selectedSources
