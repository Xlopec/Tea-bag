package com.oliynick.max.reader.app.feature.filter

import com.oliynick.max.reader.app.FullScreen
import com.oliynick.max.reader.app.ScreenId
import com.oliynick.max.reader.app.domain.Filter
import com.oliynick.max.reader.app.domain.Query
import com.oliynick.max.reader.app.domain.Source
import com.oliynick.max.reader.app.misc.Loadable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

typealias SourcesState = Loadable<PersistentList<Source>>

data class SuggestState(
    override val id: ScreenId,
    val filter: Filter,
    val sourcesState: SourcesState,
    val suggestions: ImmutableList<Query> = persistentListOf(),
) : FullScreen {
    companion object {
        const val StoreSuggestionsLimit = 10U
    }
}
