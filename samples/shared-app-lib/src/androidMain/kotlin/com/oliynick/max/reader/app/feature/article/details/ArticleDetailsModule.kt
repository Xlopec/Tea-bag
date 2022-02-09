@file:JvmName("AndroidArticleDetailsModule")
@file:Suppress("FunctionName")

package com.oliynick.max.reader.app.feature.article.details

import android.app.Application

fun ArticleDetailsModule(
    application: Application
): ArticleDetailsModule = ArticleDetailsResolver(application)