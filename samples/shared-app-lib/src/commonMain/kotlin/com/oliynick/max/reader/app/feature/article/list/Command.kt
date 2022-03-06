package com.oliynick.max.reader.app.feature.article.list

import com.oliynick.max.reader.app.ScreenId
import com.oliynick.max.reader.app.command.Command
import com.oliynick.max.reader.app.domain.Article
import kotlin.jvm.JvmInline

sealed interface ArticlesCommand : Command

data class LoadArticlesByFilter(
    val id: ScreenId,
    val filter: Filter,
    val paging: Paging
) : ArticlesCommand

@JvmInline
value class StoreSearchFilter(
    val filter: Filter
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