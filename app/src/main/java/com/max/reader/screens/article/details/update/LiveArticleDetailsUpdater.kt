package com.max.reader.screens.article.details.update

import com.max.reader.app.ArticleDetailsCommand
import com.max.reader.app.DoOpenArticle
import com.max.reader.screens.article.details.ArticleDetailsMessage
import com.max.reader.screens.article.details.ArticleDetailsState
import com.max.reader.screens.article.details.OpenInBrowser
import com.oliynick.max.tea.core.component.UpdateWith
import com.oliynick.max.tea.core.component.command

object LiveArticleDetailsUpdater : ArticleDetailsUpdater {

    override fun update(
        message: ArticleDetailsMessage,
        screen: ArticleDetailsState,
    ): UpdateWith<ArticleDetailsState, ArticleDetailsCommand> =
        when(message) {
            is OpenInBrowser -> screen command DoOpenArticle(screen.article)
        }
}