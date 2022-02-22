package com.max.reader.app.ui.screens.article

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.max.reader.app.ui.theme.ThemedPreview
import com.oliynick.max.entities.shared.now
import com.oliynick.max.reader.app.domain.Article
import com.oliynick.max.reader.app.domain.Author
import com.oliynick.max.reader.app.domain.Description
import com.oliynick.max.reader.app.domain.Title
import com.oliynick.max.reader.app.feature.*
import com.oliynick.max.reader.app.feature.article.list.ArticlesState
import com.oliynick.max.reader.app.feature.article.list.Query
import com.oliynick.max.reader.app.feature.article.list.QueryType
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
                Query("some input text", QueryType.Trending),
                LoadableState(listOf(), false, Preview)
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
        Message(
            modifier = Modifier,
            message = "Oops, something went wrong",
            actionText = "Retry",
            onClick = {}
        )
    }
}

@Composable
@ComposePreview("Articles screen preview")
fun ArticlesScreenPreview() {
    ThemedPreview {
        ArticlesScreen(
            state = ArticlesState(
                QueryType.Trending,
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
                QueryType.Trending,
                listOf(PreviewArticle),
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
                QueryType.Trending,
                listOf(),
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
                QueryType.Trending,
                listOf(PreviewArticle),
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
    type: QueryType,
    articles: List<Article>,
    transientState: TransientState
) = ArticlesState(UUID.randomUUID(), Query("input", type), LoadableState(articles, false, transientState))

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

private val PreviewArticles = listOf(
    PreviewArticle.copy(url = URL("https://miro.medium.com/1")),
    PreviewArticle.copy(url = URL("https://miro.medium.com/2")),
    PreviewArticle.copy(url = URL("https://miro.medium.com/3"))
)

private val ListState = LazyListState(firstVisibleItemIndex = 0, firstVisibleItemScrollOffset = 5)
