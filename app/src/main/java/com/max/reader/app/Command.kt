package com.max.reader.app

import com.max.reader.domain.Article
import com.max.reader.screens.article.list.Query

sealed class Command

// App wide commands

object CloseApp : Command()

// Article details commands

sealed class ArticleDetailsCommand : Command()

data class DoOpenArticle(
    val article: Article
) : ArticleDetailsCommand()

// Feed screen commands

sealed class ArticlesCommand : Command()

data class LoadByCriteria(
    val id: ScreenId,
    val query: Query
) : ArticlesCommand()

data class SaveArticle(
    val article: Article
) : ArticlesCommand()

data class RemoveArticle(
    val article: Article
) : ArticlesCommand()

data class DoShareArticle(
    val article: Article
) : ArticlesCommand()
