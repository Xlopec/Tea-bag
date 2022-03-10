@file:Suppress("FunctionName")

package com.oliynick.max.reader.app.feature.filter

import com.oliynick.max.reader.app.feature.article.list.NewsApi
import com.oliynick.max.reader.app.feature.storage.LocalStorage

typealias SuggestionsModule<Env> = SuggestionsResolver<Env>

fun <Env> SuggestionsModule(): SuggestionsModule<Env> where Env : LocalStorage, Env : NewsApi =
    SuggestionsResolver()