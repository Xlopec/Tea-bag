package com.max.reader.app.ui.screens.article

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.max.reader.app.ui.theme.ThemedPreview
import com.oliynick.max.entities.shared.now
import com.oliynick.max.reader.article.list.ArticlesState
import com.oliynick.max.reader.article.list.Query
import com.oliynick.max.reader.article.list.QueryType
import com.oliynick.max.reader.domain.Article
import com.oliynick.max.reader.domain.Author
import com.oliynick.max.reader.domain.Description
import com.oliynick.max.reader.domain.Title
import java.net.URL
import java.util.*

@Composable
@Preview("Articles search input field")
fun ArticleSearchHeaderPreview() {
    ThemedPreview {
        ArticleSearchHeader(
            state = ArticlesState(
                UUID.randomUUID(),
                Query("some input text", QueryType.Trending),
                listOf(),
                false,
                ArticlesState.TransientState.Preview
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
        Message(
            modifier = Modifier,
            message = "Oops, something went wrong",
            actionText = "Retry",
            onClick = {}
        )
    }
}

@Composable
@Preview("Articles screen preview")
fun ArticlesScreenPreview() {
    ThemedPreview {
        ArticlesScreen(
            state = ArticlesState(
                QueryType.Trending,
                PreviewArticles,
                ArticlesState.TransientState.Preview
            ),
            {}
        )
    }
}

@Composable
@Preview("Articles screen loading next")
fun ArticlesScreenLoadingNextPreview() {
    ThemedPreview {
        ArticlesScreen(
            state = ArticlesState(
                QueryType.Trending,
                listOf(PreviewArticle),
                ArticlesState.TransientState.LoadingNext
            ),
            {}
        )
    }
}

@Composable
@Preview("Articles screen loading")
fun ArticlesScreenLoadingPreview() {
    ThemedPreview {
        ArticlesScreen(
            state = ArticlesState(
                QueryType.Trending,
                listOf(),
                ArticlesState.TransientState.Loading
            ),
            {}
        )
    }
}

@Composable
@Preview("Articles screen refreshing")
fun ArticlesScreenRefreshingPreview() {
    ThemedPreview {
        ArticlesScreen(
            state = ArticlesState(
                QueryType.Trending,
                listOf(PreviewArticle),
                ArticlesState.TransientState.Refreshing
            ),
            {}
        )
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
    type: QueryType,
    articles: List<Article>,
    transientState: ArticlesState.TransientState
) = ArticlesState(UUID.randomUUID(), Query("input", type), articles, false, transientState)

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
