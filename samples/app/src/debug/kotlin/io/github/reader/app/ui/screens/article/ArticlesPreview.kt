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

package io.github.reader.app.ui.screens.article

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.github.xlopec.reader.app.feature.article.list.ArticlesState
import io.github.xlopec.reader.app.misc.*
import io.github.xlopec.reader.app.model.*
import io.github.xlopec.reader.app.ui.misc.ColumnMessage
import io.github.xlopec.reader.app.ui.screens.article.ArticleActions
import io.github.xlopec.reader.app.ui.screens.article.ArticleItem
import io.github.xlopec.reader.app.ui.screens.article.ArticleSearchHeader
import io.github.xlopec.reader.app.ui.screens.article.ArticlesScreen
import io.github.xlopec.reader.app.ui.theme.ThemedPreview
import io.github.xlopec.tea.data.now
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import java.net.URI
import java.util.*

@Composable
@Preview("Articles search input field")
fun ArticleSearchHeaderPreview() {
    ThemedPreview {
        ArticleSearchHeader(
            state = ArticlesState(
                UUID.randomUUID(),
                Filter(FilterType.Trending, Query.of("some input text")),
                Loadable(persistentListOf(), false, Idle)
            ),
            onMessage = {}
        )
    }
}

@Composable
@Preview("Articles bottom action menu")
fun ArticleActionsPreview() {
    ThemedPreview {
        ArticleActions(
            onMessage = {},
            article = PreviewArticle,
            screenId = UUID.randomUUID()
        )
    }
}

@Composable
@Preview("Messages preview")
fun MessagePreview() {
    ThemedPreview {
        ColumnMessage(
            modifier = Modifier,
            title = "Oops, something went wrong",
            message = "Unknown exception",
        ) {}
    }
}

@Composable
@Preview("Articles screen preview")
fun ArticlesScreenPreview() {
    ThemedPreview {
        ArticlesScreen(
            state = ArticlesState(
                FilterType.Trending,
                PreviewArticles,
                Idle
            ),
            ListState,
            Modifier
        ) {}
    }
}

@Composable
@Preview("Articles screen loading next")
fun ArticlesScreenLoadingNextPreview() {
    ThemedPreview {
        ArticlesScreen(
            state = ArticlesState(
                FilterType.Trending,
                persistentListOf(PreviewArticle),
                LoadingNext
            ),
            ListState,
            Modifier
        ) {}
    }
}

@Composable
@Preview("Articles screen loading")
fun ArticlesScreenLoadingPreview() {
    ThemedPreview {
        ArticlesScreen(
            state = ArticlesState(
                FilterType.Trending,
                persistentListOf(),
                Loading
            ),
            ListState,
            Modifier
        ) {}
    }
}

@Composable
@Preview("Articles screen refreshing")
fun ArticlesScreenRefreshingPreview() {
    ThemedPreview {
        ArticlesScreen(
            state = ArticlesState(
                FilterType.Trending,
                persistentListOf(PreviewArticle),
                Refreshing
            ),
            ListState,
            Modifier
        ) {}
    }
}

@Composable
@Preview("Article item")
fun ArticleItemPreview() {
    ThemedPreview {
        ArticleItem(
            screenId = UUID.randomUUID(),
            article = PreviewArticle,
            onMessage = {}
        )
    }
}

private fun ArticlesState(
    type: FilterType,
    articles: PersistentList<Article>,
    loadableState: LoadableState
) = ArticlesState(UUID.randomUUID(), Filter(type, Query.of("input")), Loadable(articles, false, loadableState))

private val PreviewArticle = Article(
    url = URI("https://www.google.com"),
    title = Title("Jetpack Compose app"),
    author = Author("Max Oliinyk"),
    description = Description(
        "Let your imagination fly! Modifiers let you modify your composable " +
                "in a very flexible way. For example, if you wanted to add some outer spacing, change " +
                "the background color of the composable, and round the corners of the Row, you could " +
                "use the following code"
    ),
    published = now(),
    isFavorite = true,
    urlToImage = URI("https://miro.medium.com/max/4000/1*Ir8CdY5D5Do5R_22Vo3uew.png"),
    source = null
)

private val PreviewArticles = persistentListOf(
    PreviewArticle.copy(url = URI("https://miro.medium.com/1")),
    PreviewArticle.copy(url = URI("https://miro.medium.com/2")),
    PreviewArticle.copy(url = URI("https://miro.medium.com/3"))
)

private val ListState = LazyListState(firstVisibleItemIndex = 0, firstVisibleItemScrollOffset = 5)
