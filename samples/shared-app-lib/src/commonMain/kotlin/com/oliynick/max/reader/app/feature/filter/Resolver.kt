@file:Suppress("FunctionName")

package com.oliynick.max.reader.app.feature.filter

import com.oliynick.max.reader.app.domain.Source
import com.oliynick.max.reader.app.feature.article.list.NewsApi
import com.oliynick.max.reader.app.feature.network.SourceResponseElement
import com.oliynick.max.reader.app.feature.storage.LocalStorage
import com.oliynick.max.reader.app.misc.mapToPersistentList
import com.oliynick.max.tea.core.component.effect
import com.oliynick.max.tea.data.Url
import com.oliynick.max.tea.data.UrlFor
import com.oliynick.max.tea.data.domain
import com.oliynick.max.tea.data.fold

interface FiltersResolver<Env> {

    suspend fun Env.resolve(
        command: FilterCommand,
    ): Set<FilterMessage>

}

fun <Env> FiltersResolver(): FiltersResolver<Env>
        where Env : LocalStorage, Env : NewsApi = FiltersResolverImpl()

private class FiltersResolverImpl<Env> : FiltersResolver<Env>
        where Env : LocalStorage, Env : NewsApi {

    override suspend fun Env.resolve(
        command: FilterCommand,
    ): Set<FilterMessage> =
        when (command) {
            is DoLoadSuggestions -> resolveRecentSearches(command)
            is DoLoadSources -> resolveSources(command)
        }
}

private suspend fun LocalStorage.resolveRecentSearches(
    command: DoLoadSuggestions,
) = command effect {
    SuggestionsLoaded(id, recentSearches(command.type))
}

private suspend fun NewsApi.resolveSources(
    command: DoLoadSources,
) = command effect {
    fetchNewsSources().fold(
        left = {
            SourcesLoaded(id,
                it.sources.mapToPersistentList(SourceResponseElement::toSource))
        },
        right = { SourcesLoadException(id, it) }
    )
}

private fun SourceResponseElement.toSource() = Source(id, name, description, url, logo)

private const val FavIconSizePx = 64

private val SourceResponseElement.logo: Url
    get() = UrlFor("https://www.google.com/s2/favicons?sz=$FavIconSizePx&domain_url=${url.domain}")
