package com.max.reader.app

import com.max.reader.domain.Article

sealed class Message

sealed class Navigation : Message()

object NavigateToFeed : Navigation()

object NavigateToFavorite : Navigation()

object NavigateToTrending : Navigation()

object Pop : Navigation()

data class NavigateToArticleDetails(
    val article: Article
) : Navigation()

abstract class ScreenMessage : Message()
