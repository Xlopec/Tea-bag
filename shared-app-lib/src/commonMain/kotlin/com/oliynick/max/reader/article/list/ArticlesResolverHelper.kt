package com.oliynick.max.reader.article.list

import com.oliynick.max.reader.app.*
import com.oliynick.max.reader.app.datatypes.Either
import com.oliynick.max.reader.app.datatypes.fold
import com.oliynick.max.reader.article.list.QueryType.*
import com.oliynick.max.reader.domain.Article
import com.oliynick.max.reader.network.Page
import com.oliynick.max.tea.core.component.effect

suspend fun <Env> Env.loadArticles(
    command: LoadArticlesByQuery
): Set<ArticlesMessage> where Env : LocalStorage, Env : NewsApi<Env> =
    command.effect {

        val (input, type) = query

        when (type) {
            Regular -> fetchFromEverything(input, currentSize, resultsPerPage).fold(command.id)
            Favorite -> findAllArticles(input).let { (articles, hasMore) ->
                ArticlesLoaded(command.id, articles, hasMore)
            }
            Trending -> fetchTopHeadlines(input, currentSize, resultsPerPage).fold(command.id)
        }
    }

suspend fun LocalStorage.storeArticle(
    article: Article,
): Set<ScreenMessage> = effect {
    insertArticle(article)
    ArticleUpdated(article)
}

suspend fun LocalStorage.removeArticle(
    article: Article,
): Set<ScreenMessage> = effect {
    deleteArticle(article.url)
    ArticleUpdated(article)
}

private fun Either<Page, AppException>.fold(
    screenId: ScreenId
) =
    fold(
        left = { (articles, hasMore) -> ArticlesLoaded(screenId, articles, hasMore) },
        right = { th -> ArticlesOperationException(screenId, th) }
    )