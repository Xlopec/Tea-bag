package com.oliynick.max.reader.article.list

import com.oliynick.max.reader.app.*
import com.oliynick.max.tea.core.component.effect

actual interface ArticlesEnv {
    val storage: LocalStorage
}

actual fun <Env> ArticlesResolver(): ArticlesResolver<Env> where Env : ArticlesEnv, Env : NewsApi<Env> =
    ArticlesResolver { command ->
        when (command) {
            is LoadArticlesByQuery -> command.effect {
                val (articles, hasMore) = when (query.type) {
                    QueryType.Regular -> fetchFromEverything(query.input, currentSize, resultsPerPage)
                    QueryType.Favorite -> storage.findAllArticles(query.input)
                    QueryType.Trending -> fetchTopHeadlines(query.input, currentSize, resultsPerPage)
                }

                ArticlesLoaded(command.id, articles, hasMore)
            }
            is SaveArticle -> command.effect {
                storage.insertArticle(article)
                ArticleUpdated(article)
            }
            is RemoveArticle -> command.effect {
                    storage.deleteArticle(article.url)
                    ArticleUpdated(article)
            }
            is DoShareArticle -> setOf()
        }
    }

actual fun ArticlesEnv(
    platform: PlatformEnv
): ArticlesEnv = object : ArticlesEnv {
    override val storage: LocalStorage = LocalStorage(platform)

}