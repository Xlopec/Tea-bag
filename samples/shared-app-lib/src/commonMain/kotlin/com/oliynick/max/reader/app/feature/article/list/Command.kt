package com.oliynick.max.reader.app.feature.article.list

import com.oliynick.max.reader.app.ScreenId
import com.oliynick.max.reader.app.command.Command
import com.oliynick.max.reader.app.domain.Article

sealed interface ArticlesCommand : Command

data class LoadArticlesByQuery(
    val id: ScreenId,
    val query: Query,
    val paging: Paging
) : ArticlesCommand

data class SaveArticle(
    val article: Article,
) : ArticlesCommand

data class RemoveArticle(
    val article: Article,
) : ArticlesCommand

data class DoShareArticle(
    val article: Article,
) : ArticlesCommand