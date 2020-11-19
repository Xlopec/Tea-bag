@file:Suppress("FunctionName")

package com.max.reader.screens.article.list.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import com.max.reader.misc.safe
import com.max.reader.screens.article.list.*
import com.max.reader.ui.theme.AppDarkThemeColors
import com.max.reader.ui.theme.ThemedPreview
import dev.chrisbanes.accompanist.coil.CoilImage
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

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
        when (state) {
            is ArticlesLoadingState -> ArticlesContent(state = state, onMessage = onMessage)
            is ArticlesPreviewState -> ArticlesContent(state = state, onMessage = onMessage)
            is ArticlesErrorState -> ArticlesContent(state = state, onMessage = onMessage)
        }.safe
    }
}

@Composable
fun ArticlesProgress(
    modifier: Modifier,
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
    state: ArticlesLoadingState,
    onMessage: (Message) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        ArticleSearchHeader(
            modifier = Modifier.padding(16.dp),
            state = state,
            onMessage = onMessage
        )

        ArticlesProgress(
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun ArticlesContent(
    state: ArticlesErrorState,
    onMessage: (Message) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        ArticleSearchHeader(
            modifier = Modifier.padding(16.dp),
            state = state,
            onMessage = onMessage
        )

        ArticlesError(
            modifier = Modifier.fillMaxSize(),
            id = state.id,
            message = state.toReadableMessage(),
            onMessage = onMessage
        )
    }
}

@Composable
fun ArticlesContent(
    state: ArticlesPreviewState,
    onMessage: (Message) -> Unit,
) {
    if (state.articles.isEmpty()) {
        ArticlesContentEmpty(
            state = state,
            onMessage = onMessage
        )
    } else {
        ArticlesContentNonEmpty(
            state = state,
            onMessage = onMessage
        )
    }
}

@Composable
fun ArticlesContentNonEmpty(
    state: ArticlesPreviewState,
    onMessage: (Message) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        if (state.criteria is LoadCriteria.Query) {
            item {
                ArticleSearchHeader(
                    id = state.id,
                    criteria = state.criteria,
                    onMessage = onMessage
                )

                Spacer(modifier = Modifier.preferredHeight(16.dp))
            }
        }

        itemsIndexed(state.articles) { index, article ->
            Column {
                ArticleItem(
                    screenId = state.id,
                    article = article,
                    onMessage = onMessage
                )

                if (index != state.articles.lastIndex) {
                    Spacer(modifier = Modifier.preferredHeight(16.dp))
                }
            }
        }
    }
}

@Composable
fun ArticleSearchHeader(
    modifier: Modifier,
    state: ArticlesState,
    onMessage: (Message) -> Unit,
) {
    ArticleSearchHeader(
        modifier = modifier,
        id = state.id,
        criteria = state.criteria as? LoadCriteria.Query ?: return,
        onMessage = onMessage
    )
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
fun ArticlesContentEmpty(
    state: ArticlesState,
    onMessage: (Message) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        ArticleSearchHeader(
            modifier = Modifier.padding(16.dp),
            state = state,
            onMessage = onMessage)

        Message(
            modifier = Modifier.fillMaxSize(),
            message = "Couldn't find articles",
            actionText = "Reload",
            onClick = {
                onMessage(LoadArticles(state.id))
            }
        )
    }
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
    modifier: Modifier = Modifier,
    id: ScreenId,
    criteria: LoadCriteria.Query,
    onMessage: (Message) -> Unit,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(all = 4.dp),
        shape = RoundedCornerShape(4.dp)
    ) {

        val caption =
            typography.subtitle2.copy(color = typography.subtitle2.color.copy(alpha = 0.4f))

        TextField(
            placeholder = { Text(text = "Search articles", style = caption) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            value = criteria.query,
            maxLines = 1,
            onImeActionPerformed = { _, ctrl ->
                onMessage(LoadArticles(id)); ctrl?.hideSoftwareKeyboard()
            },
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

private fun ArticlesErrorState.toReadableMessage() =
    cause.message?.decapitalize(Locale.getDefault()) ?: "unknown exception"

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
