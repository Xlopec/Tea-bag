@file:Suppress("FunctionName")

package com.max.weatherviewer.screens.feed.resolve

import android.content.Intent
import android.net.Uri
import com.max.weatherviewer.app.DoOpenArticle
import com.max.weatherviewer.app.DoShareArticle
import com.max.weatherviewer.app.FeedCommand
import com.max.weatherviewer.app.LoadByCriteria
import com.max.weatherviewer.app.RemoveArticle
import com.max.weatherviewer.app.SaveArticle
import com.max.weatherviewer.app.ScreenId
import com.max.weatherviewer.app.ScreenMessage
import com.max.weatherviewer.app.env.HasAppContext
import com.max.weatherviewer.app.env.storage.HasGson
import com.max.weatherviewer.app.env.storage.Storage
import com.max.weatherviewer.app.exception.AppException
import com.max.weatherviewer.app.exception.toAppException
import com.max.weatherviewer.domain.Article
import com.max.weatherviewer.screens.feed.ArticleUpdated
import com.max.weatherviewer.screens.feed.ArticlesLoaded
import com.max.weatherviewer.screens.feed.FeedOperationException
import com.max.weatherviewer.screens.feed.LoadCriteria
import com.oliynick.max.tea.core.component.effect
import com.oliynick.max.tea.core.component.sideEffect

fun <Env> LiveFeedResolver(): FeedResolver<Env> where Env : HasAppContext,
                                                      Env : HasGson,
                                                      Env : Storage<Env> = object : LiveFeedResolver<Env> {}

interface LiveFeedResolver<Env> : FeedResolver<Env> where Env : HasAppContext,
                                                          Env : HasGson,
                                                          Env : Storage<Env> {

    override suspend fun Env.resolve(
        command: FeedCommand
    ): Set<ScreenMessage> {

        suspend fun resolve() =
            when (command) {
                is LoadByCriteria -> fetch(command.id, command.criteria)
                is SaveArticle -> store(command.article)
                is RemoveArticle -> remove(command.article)
                is DoOpenArticle -> openArticle(command)
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

    suspend fun Env.openArticle(
        command: DoOpenArticle
    ): Set<ScreenMessage> = command.sideEffect {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(article.url.toString()))
            .apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }

        if (intent.resolveActivity(application.packageManager) != null) {
            application.startActivity(intent)
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
        Intent(Intent.ACTION_SEND)
            .apply {
                putExtra(Intent.EXTRA_SUBJECT, "Sharing URL")
                putExtra(Intent.EXTRA_TEXT, "http://www.url.com")
                type = "text/plain"
            }.let { intent ->
                Intent.createChooser(intent, "Share URL")
                    .apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
            }

    fun toErrorMessage(
        th: AppException,
        command: FeedCommand
    ) = FeedOperationException(
        command.screenId(),
        th
    )

}

private fun FeedCommand.screenId(): ScreenId? =
    when (this) {
        is LoadByCriteria -> id
        is SaveArticle, is RemoveArticle, is DoOpenArticle, is DoShareArticle -> null
    }
