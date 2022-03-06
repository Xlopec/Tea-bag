/*
 * MIT License
 *
 * Copyright (c) 2021. Maksym Oliinyk.
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

package com.oliynick.max.reader.app.feature.article.list

import com.oliynick.max.reader.app.AppException
import com.oliynick.max.reader.app.ScreenId
import com.oliynick.max.reader.app.TabScreen
import com.oliynick.max.reader.app.domain.Article
import com.oliynick.max.reader.app.feature.network.SourceId
import com.oliynick.max.reader.app.misc.*
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf

enum class FilterType {
    Regular, Favorite, Trending
}

data class Filter(
    val input: String,
    val type: FilterType,
    val sources: ImmutableSet<SourceId> = persistentSetOf(),
)

data class ScrollState(
    val firstVisibleItemIndex: Int,
    val firstVisibleItemScrollOffset: Int,
) {
    companion object {
        val Initial = ScrollState(firstVisibleItemIndex = 0, firstVisibleItemScrollOffset = 0)
    }
}

typealias ArticlesLoadable = Loadable<PersistentList<Article>>

data class ArticlesState(
    override val id: ScreenId,
    val filter: Filter,
    val loadable: ArticlesLoadable,
    val scrollState: ScrollState = ScrollState.Initial,
) : TabScreen {

    companion object {

        const val ArticlesPerPage = 10

        fun newLoading(
            id: ScreenId,
            filter: Filter,
            articles: PersistentList<Article> = persistentListOf(),
        ) = ArticlesState(id, filter, Loadable.newLoading(articles))
    }

}

// todo replace with immutable collection
fun ArticlesState.toLoadingNext() =
    copy(loadable = loadable.toLoadingNext())

fun ArticlesState.toLoading() =
    copy(loadable = loadable.toLoading())

fun ArticlesState.toRefreshing() =
    copy(loadable = loadable.toRefreshing())

fun ArticlesState.toPreview(
    page: Page<Article>,
): ArticlesState = copy(loadable = loadable.toPreview(page))

fun ArticlesState.toException(
    cause: AppException,
) = copy(loadable = loadable.toException(cause))

fun ArticlesState.updateArticle(
    new: Article,
): ArticlesState =
    copy(loadable = loadable.updated { articles -> articles.replace(new) { it.url == new.url } })

fun ArticlesState.prependArticle(
    new: Article,
): ArticlesState = copy(loadable = loadable.updated { it.add(0, new) })

fun ArticlesState.removeArticle(
    victim: Article,
): ArticlesState =
    copy(loadable = loadable.updated { articles -> articles.remove { it.url != victim.url } })

inline fun <E> PersistentList<E>.replace(
    e: E,
    predicate: (E) -> Boolean,
): PersistentList<E> {
    val i = indexOfFirst(predicate)

    return if (i >= 0) {
        set(i, e)
    } else {
        this
    }
}

inline fun <E> PersistentList<E>.remove(
    predicate: (E) -> Boolean,
): PersistentList<E> {
    val i = indexOfFirst(predicate)

    return if (i >= 0) {
        removeAt(i)
    } else {
        this
    }
}
