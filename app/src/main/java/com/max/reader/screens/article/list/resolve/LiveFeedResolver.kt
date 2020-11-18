@file:Suppress("FunctionName")

package com.max.reader.screens.article.list.resolve

import android.content.Intent
import com.max.reader.app.*
import com.max.reader.app.env.HasAppContext
import com.max.reader.app.env.storage.HasGson
import com.max.reader.app.env.storage.Storage
import com.max.reader.app.exception.AppException
import com.max.reader.app.exception.toAppException
import com.max.reader.domain.Article
import com.max.reader.screens.article.list.ArticleUpdated
import com.max.reader.screens.article.list.ArticlesLoaded
import com.max.reader.screens.article.list.ArticlesOperationException
import com.max.reader.screens.article.list.LoadCriteria
import com.oliynick.max.tea.core.component.effect
import com.oliynick.max.tea.core.component.sideEffect

@Deprecated("wait until it'll be fixed")
fun <Env> LiveArticlesResolver(): ArticlesResolver<Env> where Env : HasAppContext,
                                                              Env : HasGson,
                                                              Env : Storage<Env> = object : ArticlesResolver<Env> {
    override suspend fun Env.resolve(
        command: ArticlesCommand
    ): Set<ScreenMessage> {

        suspend fun resolve() =
            when (command) {
                is LoadByCriteria -> fetch(command.id, command.criteria)
                is SaveArticle -> store(command.article)
                is RemoveArticle -> remove(command.article)
                is DoShareArticle -> shareArticle(command)
            }

        return runCatching { resolve() }
            .getOrElse { th -> setOf(toErrorMessage(toAppException(th), command)) }
    }

    suspend fun Env.store(
        article: Article
    ): Set<ScreenMessage> = effect {
        addToFavorite(article)
        ArticleUpdated(
            article
        )
    }

    suspend fun Env.remove(
        article: Article
    ): Set<ScreenMessage> = effect {
        removeFromFavorite(article.url)
        ArticleUpdated(
            article
        )
    }

    suspend fun Env.fetch(
        id: ScreenId,
        criteria: LoadCriteria
    ): Set<ScreenMessage> = when (criteria) {
        is LoadCriteria.Query -> criteria.effect {
            ArticlesLoaded(
                id,
                fetch(criteria)
            )
        }
        LoadCriteria.Favorite -> criteria.effect {
            ArticlesLoaded(
                id,
                fetchFavorite()
            )
        }
        LoadCriteria.Trending -> criteria.effect {
            ArticlesLoaded(
                id,
                fetchTrending()
            )
        }
    }

    suspend fun Env.shareArticle(
        command: DoShareArticle
    ): Set<ScreenMessage> = command.sideEffect {
        application.startActivity(createShareIntent(article))
    }

    fun createShareIntent(
        article: Article
    ): Intent =
        Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, article.url.toExternalForm())
            type = "text/plain"
            putExtra(Intent.EXTRA_TITLE, article.title.value)
        }.let { intent ->
            Intent.createChooser(intent, null)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

    fun toErrorMessage(
        th: AppException,
        command: ArticlesCommand
    ) = ArticlesOperationException(
        command.screenId(),
        th
    )

}

private fun ArticlesCommand.screenId(): ScreenId? =
    when (this) {
        is LoadByCriteria -> id
        is SaveArticle, is RemoveArticle, is DoShareArticle -> null
    }
