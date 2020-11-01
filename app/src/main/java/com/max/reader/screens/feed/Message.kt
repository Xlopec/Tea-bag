package com.max.reader.screens.feed

import com.max.reader.app.ScreenId
import com.max.reader.app.ScreenMessage
import com.max.reader.app.exception.AppException
import com.max.reader.domain.Article

sealed class FeedMessage : ScreenMessage()

data class LoadArticles(
    val id: ScreenId
) : FeedMessage()

data class ToggleArticleIsFavorite(
    val id: ScreenId,
    val article: Article
) : FeedMessage()

data class ArticlesLoaded(
    val id: ScreenId,
    val articles: List<Article>
) : FeedMessage()

data class FeedOperationException(
    val id: ScreenId?,
    val cause: AppException
) : FeedMessage()

data class ArticleUpdated(
    val article: Article
) : FeedMessage()

data class OpenArticle(
    val article: Article
) : FeedMessage()

data class ShareArticle(
    val article: Article
) : FeedMessage()

data class OnQueryUpdated(
    val id: ScreenId,
    val query: String
) : FeedMessage()
