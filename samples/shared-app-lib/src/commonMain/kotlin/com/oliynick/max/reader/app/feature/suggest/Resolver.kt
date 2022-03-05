@file:Suppress("FunctionName")

package com.oliynick.max.reader.app.feature.suggest

import com.oliynick.max.entities.shared.Url
import com.oliynick.max.entities.shared.UrlFor
import com.oliynick.max.entities.shared.datatypes.fold
import com.oliynick.max.entities.shared.domain
import com.oliynick.max.reader.app.feature.article.list.NewsApi
import com.oliynick.max.reader.app.feature.network.Source
import com.oliynick.max.reader.app.feature.network.SourceResponseElement
import com.oliynick.max.reader.app.feature.storage.LocalStorage
import com.oliynick.max.tea.core.component.effect
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

interface SuggestionsResolver<Env> {

    suspend fun Env.resolve(
        command: SuggestCommand
    ): Set<SuggestMessage>

}

fun <Env> SuggestionsResolver(): SuggestionsResolver<Env> where Env : LocalStorage, Env : NewsApi =
    SuggestionsResolverImpl()

private class SuggestionsResolverImpl<Env> : SuggestionsResolver<Env>
        where Env : LocalStorage, Env : NewsApi {

    override suspend fun Env.resolve(
        command: SuggestCommand
    ): Set<SuggestMessage> =
        when (command) {
            is DoLoadSuggestions -> resolveSuggestions(command)
            is DoLoadSources -> resolveSources(command)
        }
}

private suspend fun LocalStorage.resolveSuggestions(
    command: DoLoadSuggestions
) = command effect {
    SuggestionsLoaded(id, recentSearches(command.type))
}

private suspend fun NewsApi.resolveSources(
    command: DoLoadSources
) = command effect {
    val sources = fetchNewsSources().fold(
        left = { it.sources.toPersistentList(SourceResponseElement::toSource) },
        right = { persistentListOf() }
    )

    SourcesLoaded(command.id, sources)
}

inline fun <T, R> Iterable<T>.toPersistentList(
    mapper: (T) -> R
): PersistentList<R> =
    persistentListOf<R>().builder().also { mapTo(it, mapper) }.build()

private fun SourceResponseElement.toSource() = Source(id, name, description, url, logo)

private const val FavIconSizePx = 64

private val SourceResponseElement.logo: Url
    get() = UrlFor("https://www.google.com/s2/favicons?sz=$FavIconSizePx&domain_url=${url.domain}")
