@file:Suppress("FunctionName")

package com.oliynick.max.reader.app.feature.suggest

import com.oliynick.max.reader.app.feature.storage.LocalStorage
import com.oliynick.max.tea.core.component.effect

interface SuggestionsResolver<Env> {

    suspend fun Env.resolve(
        command: SuggestCommand
    ): Set<SuggestMessage>

}

fun <Env> SuggestionsResolver(): SuggestionsResolver<Env> where Env : LocalStorage =
    SuggestionsResolverImpl()

private class SuggestionsResolverImpl<Env>(

) : SuggestionsResolver<Env> where Env : LocalStorage {

    override suspend fun Env.resolve(
        command: SuggestCommand
    ): Set<SuggestMessage> =
        when(command) {
            is DoLoadSuggestions -> command effect { SuggestionsLoaded(id, recentSearches(command.type)) }
        }

}