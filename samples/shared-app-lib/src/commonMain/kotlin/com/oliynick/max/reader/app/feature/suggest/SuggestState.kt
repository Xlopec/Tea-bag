package com.oliynick.max.reader.app.feature.suggest

import com.oliynick.max.reader.app.FullScreen
import com.oliynick.max.reader.app.ScreenId
import com.oliynick.max.reader.app.feature.article.list.Query
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class TextFieldState(
    val query: Query,
    val cursorPosition: Int
)

data class SuggestState(
    override val id: ScreenId,
    val textFieldState: TextFieldState,
    val suggestions: ImmutableList<String> = persistentListOf(),
) : FullScreen