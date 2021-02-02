package com.max.reader.screens.article.list.update

import com.max.reader.app.Command
import com.max.reader.screens.article.list.ArticlesMessage
import com.max.reader.screens.article.list.ArticlesState
import com.oliynick.max.tea.core.component.UpdateWith

interface ArticlesUpdater {
    fun updateArticles(
        message: ArticlesMessage,
        state: ArticlesState
    ): UpdateWith<ArticlesState, Command>
}
