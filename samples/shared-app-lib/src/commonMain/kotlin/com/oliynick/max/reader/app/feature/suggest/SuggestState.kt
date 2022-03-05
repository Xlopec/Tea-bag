package com.oliynick.max.reader.app.feature.suggest

import com.oliynick.max.reader.app.FullScreen
import com.oliynick.max.reader.app.ScreenId
import com.oliynick.max.reader.app.feature.article.list.Query
import com.oliynick.max.reader.app.feature.network.Source
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class TextFieldState(
    val query: Query,
    val cursorPosition: Int
)

data class SuggestState(
    override val id: ScreenId,
    val textFieldState: TextFieldState,
    val sources: ImmutableList<Source> = persistentListOf(),
    val suggestions: ImmutableList<String> = persistentListOf(),
) : FullScreen