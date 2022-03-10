@file:Suppress("FunctionName")

package com.oliynick.max.reader.app.feature.suggest

import com.oliynick.max.reader.app.ScreenId
import com.oliynick.max.reader.app.feature.article.list.Filter
import com.oliynick.max.reader.app.misc.Loadable
import com.oliynick.max.tea.core.component.command

fun SuggestionsInitialUpdate(
    id: ScreenId,
    filter: Filter,
) = SuggestState(id, filter, Loadable.newLoading())
    .command(DoLoadSuggestions(id, filter.type), DoLoadSources(id))
