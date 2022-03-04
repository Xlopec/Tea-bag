@file:Suppress("FunctionName")

package com.oliynick.max.reader.app.feature.suggest

import com.oliynick.max.reader.app.feature.storage.LocalStorage

typealias SuggestionsModule<Env> = SuggestionsResolver<Env>

fun <Env> SuggestionsModule(): SuggestionsModule<Env> where Env : LocalStorage = SuggestionsResolver()