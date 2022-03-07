package com.oliynick.max.reader.app.feature.article.list

import com.oliynick.max.reader.app.ScreenId
import com.oliynick.max.reader.app.command.Command
import com.oliynick.max.reader.app.domain.Article
import kotlin.jvm.JvmInline

sealed interface ArticlesCommand : Command

data class DoLoadArticles(
    val id: ScreenId,
    val filter: Filter,
    val paging: Paging
) : ArticlesCommand

data class DoLoadFilter(
    val id: ScreenId,
    val type: FilterType
) : ArticlesCommand

@JvmInline
value class DoStoreFilter(
    val filter: Filter
) : ArticlesCommand

data class DoSaveArticle(
    val article: Article,
) : ArticlesCommand

data class DoRemoveArticle(
    val article: Article,
) : ArticlesCommand

data class DoShareArticle(
    val article: Article,
) : ArticlesCommand