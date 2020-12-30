@file:Suppress("FunctionName")

package com.max.reader.screens.article.details

import com.max.reader.app.env.HasAppContext
import com.max.reader.app.resolve.HasCommandTransport
import com.max.reader.screens.article.details.resolve.ArticleDetailsResolver
import com.max.reader.screens.article.details.resolve.LiveArticleDetailsResolver
import com.max.reader.screens.article.details.update.ArticleDetailsUpdater
import com.max.reader.screens.article.details.update.LiveArticleDetailsUpdater
import com.max.reader.screens.article.list.resolve.ArticlesResolver

interface ArticleDetailsModule<Env> : ArticleDetailsUpdater, ArticleDetailsResolver<Env>

fun <Env> ArticleDetailsModule(): ArticleDetailsModule<Env> where Env : HasCommandTransport,
                                                                  Env : ArticlesResolver<Env>,
                                                                  Env : HasAppContext,
                                                                  Env : ArticleDetailsResolver<Env> =
    object : ArticleDetailsModule<Env>,
        ArticleDetailsUpdater by LiveArticleDetailsUpdater,
        ArticleDetailsResolver<Env> by LiveArticleDetailsResolver() {
    }
