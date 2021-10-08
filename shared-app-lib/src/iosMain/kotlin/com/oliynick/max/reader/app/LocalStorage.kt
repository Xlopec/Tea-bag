package com.oliynick.max.reader.app

import com.oliynick.max.reader.domain.Article
import com.oliynick.max.reader.domain.Url
import com.oliynick.max.reader.network.Page

actual fun LocalStorage(platform: PlatformEnv): LocalStorage =
    object : LocalStorage {
        override suspend fun insertArticle(article: Article) {

        }

        override suspend fun deleteArticle(url: Url) {
        }

        override suspend fun findAllArticles(input: String): Page =
            Page(listOf(), false)

        override suspend fun isFavoriteArticle(url: Url): Boolean =
            false

        override suspend fun isDarkModeEnabled(): Boolean =
            false

        override suspend fun storeIsDarkModeEnabled(isEnabled: Boolean) {
        }

    }