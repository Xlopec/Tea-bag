@file:Suppress("FunctionName")

package com.oliynick.max.reader.article.list

import com.oliynick.max.reader.app.*

fun <Env> ArticlesResolver(): ArticlesResolver<Env> where Env : LocalStorage, Env : NewsApi =
    object : ArticlesResolver<Env> {
        override suspend fun Env.resolve(
            command: ArticlesCommand
        ): Set<Message> =
            when (command) {
                is LoadArticlesByQuery -> loadArticles(command)
                is SaveArticle -> storeArticle(command.article)
                is RemoveArticle -> removeArticle(command.article)
                is DoShareArticle -> setOf<ScreenMessage>()
            }
    }