@file:Suppress("FunctionName")

package com.oliynick.max.reader.article.details

expect interface ArticleDetailsEnv

expect fun <Env : ArticleDetailsEnv> ArticleDetailsResolver(): ArticleDetailsResolver<Env>