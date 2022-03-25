@file:Suppress("FunctionName")

package com.oliynick.max.reader.app.feature.filter

import com.oliynick.max.reader.app.ScreenId
import com.oliynick.max.reader.app.domain.Filter
import com.oliynick.max.reader.app.misc.Loadable
import com.oliynick.max.tea.core.component.command

fun FiltersInitialUpdate(
    id: ScreenId,
    filter: Filter,
) = FiltersState(id, filter, Loadable.newLoading())
    .command(DoLoadSuggestions(id, filter.type), DoLoadSources(id))