package com.oliynick.max.reader.article.list

import com.oliynick.max.reader.network.NewsApiImpl

fun NewsApi(): NewsApi = NewsApiImpl("EN")