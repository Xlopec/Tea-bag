package com.oliynick.max.reader.article.list

expect interface ArticlesEnv

expect fun <Env> ArticlesResolver(): ArticlesResolver<Env> where Env : ArticlesEnv, Env : NewsApi<Env>