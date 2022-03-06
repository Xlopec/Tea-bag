package com.max.reader.app.ui.screens.article

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.max.reader.app.ui.misc.ColumnMessage
import com.max.reader.app.ui.theme.ThemedPreview
import com.oliynick.max.entities.shared.now
import com.oliynick.max.reader.app.domain.Article
import com.oliynick.max.reader.app.domain.Author
import com.oliynick.max.reader.app.domain.Description
import com.oliynick.max.reader.app.domain.Title
import com.oliynick.max.reader.app.feature.article.list.ArticlesState
import com.oliynick.max.reader.app.feature.article.list.Filter
import com.oliynick.max.reader.app.feature.article.list.FilterType
import com.oliynick.max.reader.app.misc.*
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import java.net.URL
import java.util.*
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

@Composable
@ComposePreview("Articles search input field")
fun ArticleSearchHeaderPreview() {
    ThemedPreview {
        ArticleSearchHeader(
            state = ArticlesState(
                UUID.randomUUID(),
                Filter("some input text", FilterType.Trending),
                Loadable(persistentListOf(), false, Preview)
            ),
            onMessage = {}
        )
    }
}

@Composable
@ComposePreview("Articles bottom action menu")
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
@ComposePreview("Messages preview")
fun MessagePreview() {
    ThemedPreview {
        ColumnMessage(
            modifier = Modifier,
            message = "Oops, something went wrong"
        ) {}
    }
}

@Composable
@ComposePreview("Articles screen preview")
fun ArticlesScreenPreview() {
    ThemedPreview {
        ArticlesScreen(
            state = ArticlesState(
                FilterType.Trending,
                PreviewArticles,
                Preview
            ),
            ListState,
            Modifier
        ) {}
    }
}

@Composable
@ComposePreview("Articles screen loading next")
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
@ComposePreview("Articles screen loading")
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
@ComposePreview("Articles screen refreshing")
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
@ComposePreview("Article item")
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
) = ArticlesState(UUID.randomUUID(), Filter("input", type), Loadable(articles, false, loadableState))

private val PreviewArticle = Article(
    url = URL("https://www.google.com"),
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
    urlToImage = URL("https://miro.medium.com/max/4000/1*Ir8CdY5D5Do5R_22Vo3uew.png")
)

private val PreviewArticles = persistentListOf(
    PreviewArticle.copy(url = URL("https://miro.medium.com/1")),
    PreviewArticle.copy(url = URL("https://miro.medium.com/2")),
    PreviewArticle.copy(url = URL("https://miro.medium.com/3"))
)

private val ListState = LazyListState(firstVisibleItemIndex = 0, firstVisibleItemScrollOffset = 5)
