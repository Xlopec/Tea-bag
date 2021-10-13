package com.oliynick.max.reader.article.list

import com.oliynick.max.reader.app.*
import com.oliynick.max.tea.core.component.effect

fun <Env> ArticlesResolver(): ArticlesResolver<Env> where Env : LocalStorage, Env : NewsApi =
    object : ArticlesResolver<Env> {
        // fixme rewrite
        override suspend fun Env.resolve(command: ArticlesCommand): Set<Message> {

            val r = when (command) {
                is LoadArticlesByQuery -> command.effect {
                    runCatching<Message> {
                        val (articles, hasMore) = when (query.type) {
                            QueryType.Regular -> fetchFromEverything(
                                query.input,
                                currentSize,
                                resultsPerPage
                            )
                            QueryType.Favorite -> findAllArticles(query.input)
                            QueryType.Trending -> fetchTopHeadlines(
                                query.input,
                                currentSize,
                                resultsPerPage
                            )
                        }

                        ArticlesLoaded(command.id, articles, hasMore)
                    }.getOrElse { th -> ArticlesOperationException(command.id, NetworkException("Couldn't load articles feed", th)) }
                }
                is SaveArticle -> command.effect {
                    insertArticle(article)
                    ArticleUpdated(article)
                }
                is RemoveArticle -> command.effect {
                    deleteArticle(article.url)
                    ArticleUpdated(article)
                }
                is DoShareArticle -> setOf()
            }

            return r
        }
    }