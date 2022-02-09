@file:Suppress("FunctionName")

package com.oliynick.max.reader.app.feature.article.details

import com.oliynick.max.reader.app.feature.article.list.ArticlesResolver

fun <Env> ArticleDetailsModule(): ArticleDetailsModule<Env>
        where Env : ArticlesResolver<Env>,
              Env : ArticleDetailsResolver =
    object : ArticleDetailsModule<Env>,
        ArticleDetailsUpdater by LiveArticleDetailsUpdater,
        ArticleDetailsResolver by ArticleDetailsResolver() {
    }