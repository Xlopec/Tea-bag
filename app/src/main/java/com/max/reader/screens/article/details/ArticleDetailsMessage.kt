package com.max.reader.screens.article.details

import com.max.reader.app.ScreenId
import com.max.reader.app.ScreenMessage

sealed class ArticleDetailsMessage : ScreenMessage()

data class OpenInBrowser(
    val id: ScreenId
) : ArticleDetailsMessage()