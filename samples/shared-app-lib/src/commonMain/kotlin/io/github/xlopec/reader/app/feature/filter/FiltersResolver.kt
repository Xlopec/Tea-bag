/*
 * MIT License
 *
 * Copyright (c) 2022. Maksym Oliinyk.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

@file:Suppress("FunctionName")

package io.github.xlopec.reader.app.feature.filter

import io.github.xlopec.reader.app.effect
import io.github.xlopec.reader.app.feature.article.list.NewsApi
import io.github.xlopec.reader.app.feature.network.SourceResponseElement
import io.github.xlopec.reader.app.feature.storage.LocalStorage
import io.github.xlopec.reader.app.misc.mapToPersistentList
import io.github.xlopec.reader.app.model.Source
import io.github.xlopec.reader.app.sideEffect
import io.github.xlopec.tea.data.Url
import io.github.xlopec.tea.data.UrlFor
import io.github.xlopec.tea.data.domain

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
            is DoLoadRecentSearches -> fetchRecentSearches(command)
            is DoLoadSources -> fetchSources(command)
            is DoRemoveRecentSearch -> removeRecentSearch(command)
        }
}

private suspend fun LocalStorage.removeRecentSearch(
    command: DoRemoveRecentSearch,
) = command sideEffect {
    deleteRecentSearch(command.type, command.query)
}

private suspend fun LocalStorage.fetchRecentSearches(
    command: DoLoadRecentSearches,
) = command effect {
    RecentSearchesLoaded(id, recentSearches(command.type))
}

private suspend fun NewsApi.fetchSources(
    command: DoLoadSources,
) = command effect {
    fetchNewsSources().fold(
        { SourcesLoadException(id, it) },
        { SourcesLoadSuccess(id, it.sources.mapToPersistentList(SourceResponseElement::toSource)) }
    )
}

private fun SourceResponseElement.toSource() = Source(id, name, description, url, logo)

private const val FavIconSizePx = 64

private val SourceResponseElement.logo: Url
    get() = UrlFor("https://www.google.com/s2/favicons?sz=$FavIconSizePx&domain_url=${url.domain}")
