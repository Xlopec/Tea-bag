package com.oliynick.max.reader.app.feature.suggest

import com.oliynick.max.reader.app.ScreenId
import com.oliynick.max.reader.app.command.Command
import com.oliynick.max.reader.app.feature.article.list.QueryType

sealed interface SuggestCommand : Command

data class DoLoadSuggestions(
    val id: ScreenId,
    val type: QueryType
) : SuggestCommand
