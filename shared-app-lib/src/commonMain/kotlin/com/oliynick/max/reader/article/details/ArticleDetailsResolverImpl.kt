@file:Suppress("FunctionName")

package com.oliynick.max.reader.article.details

import com.oliynick.max.reader.app.PlatformEnv

expect interface ArticleDetailsEnv

expect fun <Env : ArticleDetailsEnv> ArticleDetailsResolver(): ArticleDetailsResolver<Env>
expect fun ArticleDetailsEnv(platformEnv: PlatformEnv): ArticleDetailsEnv