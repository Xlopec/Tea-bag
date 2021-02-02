package com.max.reader.screens.article.details

import com.max.reader.app.ScreenId
import com.max.reader.app.ScreenState
import com.max.reader.domain.Article

data class ArticleDetailsState(
    override val id: ScreenId,
    val article: Article,
) : ScreenState()
