@file:Suppress("FunctionName")

package com.oliynick.max.reader.article.details

import com.oliynick.max.reader.article.list.ArticlesResolver

fun <Env> ArticleDetailsModule(): ArticleDetailsModule<Env>
        where Env : ArticlesResolver<Env>,
              Env : ArticleDetailsResolver =
    object : ArticleDetailsModule<Env>,
        ArticleDetailsUpdater by LiveArticleDetailsUpdater,
        ArticleDetailsResolver by ArticleDetailsResolver() {
    }