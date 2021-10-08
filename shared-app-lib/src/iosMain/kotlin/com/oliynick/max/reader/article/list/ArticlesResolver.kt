package com.oliynick.max.reader.article.list



actual interface ArticlesEnv

actual fun <Env : ArticlesEnv> ArticlesResolver(): ArticlesResolver<Env> where Env : NewsApi<Env> {
    TODO("Not yet implemented")
}