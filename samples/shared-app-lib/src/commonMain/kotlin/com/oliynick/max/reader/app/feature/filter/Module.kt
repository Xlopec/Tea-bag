@file:Suppress("FunctionName")

package com.oliynick.max.reader.app.feature.filter

import com.oliynick.max.reader.app.feature.article.list.NewsApi
import com.oliynick.max.reader.app.feature.storage.LocalStorage

typealias FiltersModule<Env> = FiltersResolver<Env>

fun <Env> FiltersModule(): FiltersModule<Env> where Env : LocalStorage, Env : NewsApi =
    FiltersResolver()