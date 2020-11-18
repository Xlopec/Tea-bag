@file:Suppress("FunctionName")

package com.max.reader.screens.article.list.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.max.reader.app.Message
import com.max.reader.app.NavigateToArticleDetails
import com.max.reader.app.ScreenId
import com.max.reader.domain.Article
import com.max.reader.domain.Author
import com.max.reader.domain.Description
import com.max.reader.domain.Title
import com.max.reader.misc.*
import com.max.reader.screens.article.list.*
import com.max.reader.ui.theme.AppDarkThemeColors
import com.max.reader.ui.theme.ThemedPreview
import dev.chrisbanes.accompanist.coil.CoilImage
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

typealias ArticleContentItem = Either5<LoadCriteria.Query, Article, Unit, ArticlesLoadingState, ArticlesLoadingError>

@Composable
fun ArticlesScreen(
    modifier: Modifier,
    state: ArticlesState,
    onMessage: (Message) -> Unit,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        alignment = Alignment.Center
    ) {
        Crossfade(current = state.id) {
            ArticlesContent(state, onMessage)
        }
    }
}

@Composable
fun ArticlesProgress(
    modifier: Modifier
) {
    Box(
        modifier = modifier,
        alignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun ArticlesContent(
    state: ArticlesState,
    onMessage: (Message) -> Unit,
) {
    LazyColumnFor(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        items = state.toContentData(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) { either ->

        ArticleContentItem(
            id = state.id,
            item = either,
            onMessage = onMessage
        )

        Spacer(modifier = Modifier.preferredHeight(12.dp))
    }
}

@Composable
fun LazyItemScope.ArticleContentItem(
    id: ScreenId,
    item: ArticleContentItem,
    onMessage: (Message) -> Unit
) {
    when (item) {
        is E0 ->
            ArticleSearchHeader(
                id = id,
                criteria = item.l,
                onMessage = onMessage
            )
        is E1 -> ArticleItem(
            screenId = id,
            article = item.r,
            onMessage = onMessage
        )
        is E2 -> ArticlesEmpty(
            modifier = Modifier.fillParentMaxSize(),
            id = id,
            onMessage = onMessage
        )
        is E3 -> ArticlesProgress(
            modifier = Modifier.fillParentMaxSize()
        )
        is E4 -> ArticlesError(
            modifier = Modifier.fillParentMaxSize(),
            id = item.r.id,
            message = item.r.toReadableMessage(),
            onMessage = onMessage
        )
    }.safe
}

@Composable
fun ArticleImage(
    imageUrl: URL?,
) {
    Surface(
        modifier = Modifier
            .preferredHeight(200.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(topLeft = 8.dp, topRight = 8.dp),
        color = colors.onSurface.copy(alpha = 0.2f)
    ) {

        if (imageUrl != null) {

            CoilImage(
                modifier = Modifier.fillMaxWidth(),
                data = imageUrl.toExternalForm(),
                fadeIn = true,
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
fun ArticleItem(
    screenId: ScreenId,
    article: Article,
    onMessage: (Message) -> Unit,
) {
    Card(
        elevation = 4.dp,
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(
            modifier = Modifier
                .clickable(onClick = { onMessage(NavigateToArticleDetails(article)) })
        ) {

            ArticleImage(imageUrl = article.urlToImage)

            Spacer(modifier = Modifier.height(8.dp))

            ArticleContents(article = article)

            Spacer(modifier = Modifier.height(4.dp))

            ArticleActions(onMessage, article, screenId)
        }
    }
}

@Composable
private fun ArticleContents(
    article: Article,
) {
    Column(modifier = Modifier.padding(horizontal = 8.dp)) {

        Text(
            text = article.title.value,
            style = typography.h6
        )

        if (article.author != null) {
            Text(
                text = article.author.value,
                style = typography.subtitle2
            )
        }

        Text(
            text = "Published on ${DateFormatter.format(article.published)}",
            style = typography.body2
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = article.description?.value ?: "No description",
            style = typography.body2
        )
    }
}

@Composable
private fun ArticleActions(
    onMessage: (Message) -> Unit,
    article: Article,
    screenId: ScreenId,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {

        IconButton(
            onClick = { onMessage(ShareArticle(article)) }
        ) {
            Icon(asset = Icons.Default.Share)
        }

        IconButton(
            onClick = { onMessage(ToggleArticleIsFavorite(screenId, article)) }
        ) {
            Icon(
                tint = if (article.isFavorite) AppDarkThemeColors.primary else AppDarkThemeColors.onSecondary,
                asset = if (article.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            )
        }
    }
}

@Composable
fun ArticlesEmpty(
    modifier: Modifier,
    id: ScreenId,
    onMessage: (Message) -> Unit,
) {
    Message(
        modifier = modifier,
        message = "Couldn't find articles",
        actionText = "Reload",
        onClick = {
            onMessage(LoadArticles(id))
        }
    )
}

@Composable
fun ArticlesError(
    modifier: Modifier,
    id: ScreenId,
    message: String,
    onMessage: (Message) -> Unit,
) {
    Message(
        modifier,
        "Failed to load articles, message: '${message.decapitalize(Locale.getDefault())}'",
        "Retry"
    ) {
        onMessage(LoadArticles(id))
    }
}

@Composable
fun Message(
    modifier: Modifier,
    message: String,
    actionText: String,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            textAlign = TextAlign.Center
        )

        TextButton(
            onClick = onClick
        ) {
            Text(text = actionText)
        }
    }
}

@Composable
fun ArticleSearchHeader(
    id: ScreenId,
    criteria: LoadCriteria.Query,
    onMessage: (Message) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = 4.dp),
        shape = RoundedCornerShape(4.dp)
    ) {
        TextField(
            value = criteria.query,
            imeAction = ImeAction.Search,
            backgroundColor = colors.surface,
            textStyle = typography.subtitle2,
            trailingIcon = {
                IconButton(
                    onClick = { onMessage(LoadArticles(id)) }
                ) {
                    Icon(asset = Icons.Default.Search)
                }
            },
            onValueChange = { query -> onMessage(OnQueryUpdated(id, query)) }
        )
    }
}

@Composable
@androidx.ui.tooling.preview.Preview(
    "Articles search input field"
)
fun ArticleSearchHeaderPreview() {
    ThemedPreview {
        ArticleSearchHeader(
            id = UUID.randomUUID(),
            criteria = LoadCriteria.Query("some input text"),
            onMessage = {}
        )
    }
}

@Composable
@androidx.ui.tooling.preview.Preview(
    "Article item"
)
fun ArticleItemPreview() {
    ThemedPreview {
        ArticleItem(
            screenId = UUID.randomUUID(),
            article = ArticleSamplePreview,
            onMessage = {}
        )
    }
}

@Composable
@androidx.ui.tooling.preview.Preview(
    "Articles bottom action menu"
)
fun ArticleActionsPreview() {
    ThemedPreview {
        ArticleActions(
            onMessage = {},
            article = ArticleSamplePreview,
            screenId = UUID.randomUUID()
        )
    }
}

@Composable
@androidx.ui.tooling.preview.Preview(
    "Messages preview"
)
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

private val DateFormatter: SimpleDateFormat by lazy {
    SimpleDateFormat("dd MMM' at 'hh:mm", Locale.getDefault())
}

private fun ArticlesLoadingError.toReadableMessage() =
    cause.message?.decapitalize(Locale.getDefault()) ?: "unknown exception"

private fun ArticlesState.toContentData(): MutableList<ArticleContentItem> {

    val m = mutableListOf<ArticleContentItem>()

    if (criteria is LoadCriteria.Query) {
        m += E0(criteria as LoadCriteria.Query)
    }

    when (this) {
        is ArticlesLoadingState -> m += E3(this)
        is ArticlesPreviewState -> {
            if (articles.isEmpty()) {
                m += E2(Unit)
            } else {
                m.addAll(articles.map(::E1))
            }
        }
        is ArticlesLoadingError -> m += E4(this)
    }

    return m
}

private val ArticleSamplePreview = Article(
    url = URL("https://www.google.com"),
    title = Title("Jetpack Compose app"),
    author = Author("Max Oliinyk"),
    description = Description("Let your imagination fly! Modifiers let you modify your composable " +
            "in a very flexible way. For example, if you wanted to add some outer spacing, change " +
            "the background color of the composable, and round the corners of the Row, you could " +
            "use the following code"),
    published = Date(),
    isFavorite = true,
    urlToImage = URL("https://miro.medium.com/max/4000/1*Ir8CdY5D5Do5R_22Vo3uew.png")
)
