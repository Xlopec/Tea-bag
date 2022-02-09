@file:Suppress("FunctionName")

package com.oliynick.max.reader.app.feature.article.details

import android.app.Application
import com.oliynick.max.reader.app.feature.article.list.ArticlesResolver

fun <Env> ArticleDetailsModule(
    application: Application
): ArticleDetailsModule<Env>
        where Env : ArticlesResolver<Env>,
              Env : ArticleDetailsResolver =
    object : ArticleDetailsModule<Env>,
        ArticleDetailsUpdater by LiveArticleDetailsUpdater,
        ArticleDetailsResolver by ArticleDetailsResolver(application) {
    }