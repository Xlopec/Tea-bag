package com.oliynick.max.reader.article.list

import com.oliynick.max.reader.app.PlatformEnv

expect interface ArticlesEnv

expect fun <Env> ArticlesResolver(): ArticlesResolver<Env> where Env : ArticlesEnv, Env : NewsApi<Env>

expect fun ArticlesEnv(platform: PlatformEnv): ArticlesEnv