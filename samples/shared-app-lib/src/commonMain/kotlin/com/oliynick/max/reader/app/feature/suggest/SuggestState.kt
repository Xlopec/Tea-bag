package com.oliynick.max.reader.app.feature.suggest

import com.oliynick.max.reader.app.FullScreen
import com.oliynick.max.reader.app.ScreenId
import com.oliynick.max.reader.app.feature.article.list.Filter
import com.oliynick.max.reader.app.feature.article.list.Query
import com.oliynick.max.reader.app.feature.network.Source
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
