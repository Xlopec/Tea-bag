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

package io.github.xlopec.reader.app.ui.screens.article

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.xlopec.reader.app.feature.article.list.ArticlesState
import io.github.xlopec.reader.app.feature.navigation.Tab
import io.github.xlopec.reader.app.misc.Idle
import io.github.xlopec.reader.app.misc.Loadable
import io.github.xlopec.reader.app.misc.LoadableState
import io.github.xlopec.reader.app.misc.Loading
import io.github.xlopec.reader.app.misc.LoadingNext
import io.github.xlopec.reader.app.misc.Refreshing
import io.github.xlopec.reader.app.model.Article
import io.github.xlopec.reader.app.model.Author
import io.github.xlopec.reader.app.model.Description
import io.github.xlopec.reader.app.model.Filter
import io.github.xlopec.reader.app.model.FilterType
import io.github.xlopec.reader.app.model.Query
import io.github.xlopec.reader.app.model.Title
import io.github.xlopec.reader.app.ui.misc.ColumnMessage
import io.github.xlopec.reader.app.ui.theme.ThemedPreview
import io.github.xlopec.tea.data.UrlFor
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.uuid.Uuid

@Composable
@Preview
internal fun ArticleSearchHeaderPreview() {
    ThemedPreview {
        ArticleSearchHeader(
            state = ArticlesState(
                tab = Tab.Trending,
                filter = Filter(FilterType.Trending, Query.of("some input text")),
                loadable = Loadable(data = persistentListOf(), hasMore = false, loadableState = Idle)
            ),
            onMessage = {}
        )
    }
}

@Composable
@Preview
internal fun ArticleActionsPreview() {
    ThemedPreview {
        ArticleActions(
            onMessage = {},
            article = PreviewArticle,
            screenId = Uuid.random()
        )
    }
}

@Composable
@Preview
internal fun MessagePreview() {
    ThemedPreview {
        ColumnMessage(
            modifier = Modifier,
            title = "Oops, something went wrong",
            message = "Unknown exception",
        ) {}
    }
}

@Composable
@Preview
internal fun ArticlesScreenPreview() {
    ThemedPreview {
        Articles(
            state = ArticlesState(
                tab = Tab.Trending,
                type = FilterType.Trending,
                articles = PreviewArticles,
                loadableState = Idle
            ),
            listState = ListState,
            modifier = Modifier
        ) {}
    }
}

@Composable
@Preview
internal fun ArticlesScreenLoadingNextPreview() {
    ThemedPreview {
        Articles(
            state = ArticlesState(
                tab = Tab.Trending,
                type = FilterType.Trending,
                articles = persistentListOf(PreviewArticle),
                loadableState = LoadingNext
            ),
            listState = ListState,
            modifier = Modifier
        ) {}
    }
}

@Composable
@Preview
internal fun ArticlesScreenLoadingPreview() {
    ThemedPreview {
        Articles(
            state = ArticlesState(
                tab = Tab.Trending,
                type = FilterType.Trending,
                articles = persistentListOf(),
                loadableState = Loading
            ),
            listState = ListState,
            modifier = Modifier
        ) {}
    }
}

@Composable
@Preview
internal fun ArticlesScreenRefreshingPreview() {
    ThemedPreview {
        Articles(
            state = ArticlesState(
                tab = Tab.Trending,
                type = FilterType.Trending,
                articles = persistentListOf(PreviewArticle),
                loadableState = Refreshing
            ),
            listState = ListState,
            modifier = Modifier
        ) {}
    }
}

@Composable
@Preview
internal fun ArticleItemPreview() {
    ThemedPreview {
        ArticleItem(
            screenId = Uuid.random(),
            article = PreviewArticle,
            onMessage = {}
        )
    }
}

private fun ArticlesState(
    tab: Tab,
    type: FilterType,
    articles: PersistentList<Article>,
    loadableState: LoadableState,
) = ArticlesState(
    tab = tab,
    filter = Filter(type, Query.of("input")),
    loadable = Loadable(
        data = articles,
        hasMore = false,
        loadableState = loadableState
    )
)

private val PreviewArticle = Article(
    url = UrlFor("https://www.google.com"),
    title = Title("Jetpack Compose app"),
    author = Author("Max Oliinyk"),
    description = Description(
        "Let your imagination fly! Modifiers let you modify your composable " +
            "in a very flexible way. For example, if you wanted to add some outer spacing, change " +
            "the background color of the composable, and round the corners of the Row, you could " +
            "use the following code"
    ),
    published = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
    isFavorite = true,
    urlToImage = UrlFor("https://miro.medium.com/max/4000/1*Ir8CdY5D5Do5R_22Vo3uew.png"),
    source = null
)

private val PreviewArticles = persistentListOf(
    PreviewArticle.copy(url = UrlFor("https://miro.medium.com/1")),
    PreviewArticle.copy(url = UrlFor("https://miro.medium.com/2")),
    PreviewArticle.copy(url = UrlFor("https://miro.medium.com/3"))
)

private val ListState = LazyListState(firstVisibleItemIndex = 0, firstVisibleItemScrollOffset = 5)
