package com.oliynick.max.reader.article.list

import com.oliynick.max.reader.app.LocalStorage
import com.oliynick.max.reader.network.NewsApiImpl

fun <Env : LocalStorage> NewsApi(): NewsApi<Env> = NewsApiImpl("EN")