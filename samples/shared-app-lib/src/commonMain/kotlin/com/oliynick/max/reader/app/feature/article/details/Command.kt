package com.oliynick.max.reader.app.feature.article.details

import com.oliynick.max.reader.app.command.Command
import com.oliynick.max.reader.app.domain.Article

sealed interface ArticleDetailsCommand : Command

data class DoOpenArticle(
    val article: Article,
) : ArticleDetailsCommand
