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

package io.github.xlopec.reader.app.feature.article.list

import io.github.xlopec.reader.app.AppException
import io.github.xlopec.reader.app.ScreenId
import io.github.xlopec.reader.app.TabScreen
import io.github.xlopec.reader.app.misc.Loadable
import io.github.xlopec.reader.app.misc.remove
import io.github.xlopec.reader.app.misc.replace
import io.github.xlopec.reader.app.misc.toException
import io.github.xlopec.reader.app.misc.toIdle
import io.github.xlopec.reader.app.misc.toLoading
import io.github.xlopec.reader.app.misc.toLoadingNext
import io.github.xlopec.reader.app.misc.toRefreshing
import io.github.xlopec.reader.app.misc.updated
import io.github.xlopec.reader.app.model.Article
import io.github.xlopec.reader.app.model.Filter
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

public typealias ArticlesLoadable = Loadable<Article>

public data class ScrollState(
    val firstVisibleItemIndex: Int,
    val firstVisibleItemScrollOffset: Int,
) {
    internal companion object {
        val Initial = ScrollState(firstVisibleItemIndex = 0, firstVisibleItemScrollOffset = 0)
    }
}

public data class ArticlesState(
    override val id: ScreenId,
    val filter: Filter,
    val loadable: ArticlesLoadable,
    val scrollState: ScrollState = ScrollState.Initial,
) : TabScreen {

    internal companion object {

        const val ArticlesPerPage = 10

        fun newLoading(
            id: ScreenId,
            filter: Filter,
            articles: PersistentList<Article> = persistentListOf(),
        ) = ArticlesState(id, filter, Loadable.newLoading(articles))
    }
}

internal fun ArticlesState.toLoadingNext() =
    copy(loadable = loadable.toLoadingNext())

internal fun ArticlesState.toLoading(
    filter: Filter = this.filter
) = copy(filter = filter, loadable = loadable.toLoading())

internal fun ArticlesState.toRefreshing() =
    copy(loadable = loadable.toRefreshing())

internal fun ArticlesState.toIdle(
    page: Page<Article>,
): ArticlesState = copy(loadable = loadable.toIdle(page))

internal fun ArticlesState.toException(
    cause: AppException,
) = copy(loadable = loadable.toException(cause))

internal fun ArticlesState.updateArticle(
    new: Article,
): ArticlesState =
    copy(loadable = loadable.updated { replace(new) { it.url == new.url } })

internal fun ArticlesState.removeArticle(
    victim: Article,
): ArticlesState =
    copy(loadable = loadable.updated { remove { it.url == victim.url } })
