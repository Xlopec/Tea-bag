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

package com.max.reader.screens.article.list.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.ButtonDefaults.textButtonColors
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.coil.rememberCoilPainter
import com.google.accompanist.insets.statusBarsPadding
import com.max.reader.app.ScreenId
import com.max.reader.app.message.Message
import com.max.reader.app.message.NavigateToArticleDetails
import com.max.reader.domain.Article
import com.max.reader.domain.Author
import com.max.reader.domain.Description
import com.max.reader.domain.Title
import com.max.reader.misc.safe
import com.max.reader.screens.article.list.*
import com.max.reader.screens.article.list.ArticlesState.TransientState.*
import com.max.reader.screens.article.list.QueryType.*
import com.max.reader.ui.theme.ThemedPreview
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.tooling.preview.Preview as Render

@Composable
fun ArticlesScreen(
    modifier: Modifier,
    state: ArticlesState,
    onMessage: (Message) -> Unit,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        val listState = listState(id = state.id)

        when (val transientState = state.transientState) {
            is Exception -> ArticlesExceptionContent(
                id = state.id,
                state = listState,
                query = state.query,
                articles = state.articles,
                cause = transientState.th,
                onMessage = onMessage
            )
            is LoadingNext,
            is Loading,
            -> ArticlesLoadingContent(
                state = listState,
                id = state.id,
                query = state.query,
                articles = state.articles,
                onMessage = onMessage
            )
            is Refreshing,
            is Preview,
            -> ArticlesPreviewContent(
                state = listState,
                id = state.id,
                query = state.query,
                articles = state.articles,
                onMessage = onMessage
            )
        }.safe
    }
}

@Composable
private fun ArticlesProgress(
    modifier: Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = colors.secondaryVariant
        )
    }
}

@Composable
private fun ArticlesLoadingContent(
    state: LazyListState,
    id: ScreenId,
    query: Query,
    articles: List<Article>,
    onMessage: (Message) -> Unit,
) {
    if (articles.isEmpty()) {
        ArticlesContent(
            id = id,
            query = query,
            onMessage = onMessage
        ) {
            ArticlesProgress(modifier = Modifier.fillMaxSize())
        }
    } else {
        ArticlesContent(
            state = state,
            id = id,
            query = query,
            onMessage = onMessage
        ) {
            ArticlesContentNonEmptyImpl(
                id = id,
                query = query,
                articles = articles,
                onMessage = onMessage
            )

            item {
                Spacer(modifier = Modifier.height(16.dp))
                ArticlesProgress(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun ArticlesExceptionContent(
    state: LazyListState,
    id: ScreenId,
    query: Query,
    articles: List<Article>,
    cause: Throwable,
    onMessage: (Message) -> Unit,
) {
    if (articles.isEmpty()) {
        ArticlesContent(id, query, onMessage) {
            ArticlesError(
                modifier = Modifier.fillMaxSize(),
                id = id,
                message = cause.toReadableMessage(),
                onMessage = onMessage
            )
        }
    } else {
        ArticlesContent(state, id, query, onMessage) {
            ArticlesContentNonEmptyImpl(id, query, articles, onMessage)

            item {
                ArticlesError(
                    modifier = Modifier.fillMaxWidth(),
                    id = id,
                    message = cause.toReadableMessage(),
                    onMessage = onMessage
                )
            }
        }

    }
}

@Composable
private fun ArticlesPreviewContent(
    state: LazyListState,
    id: ScreenId,
    query: Query,
    articles: List<Article>,
    onMessage: (Message) -> Unit,
) {
    if (articles.isEmpty()) {
        ArticlesContent(id, query, onMessage) {
            Message(
                modifier = Modifier.fillMaxSize(),
                message = "No articles",
                actionText = "Reload",
                onClick = {
                    onMessage(LoadArticlesFromScratch(id))
                }
            )
        }
    } else {
        ArticlesContent(state, id, query, onMessage) {
            ArticlesContentNonEmptyImpl(id, query, articles, onMessage)
        }
    }
}

@Composable
private fun ArticlesContent(
    id: ScreenId,
    query: Query,
    onMessage: (Message) -> Unit,
    children: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ArticleSearchHeader(
            id = id,
            query = query,
            onMessage = onMessage
        )
        Spacer(modifier = Modifier.height(16.dp))
        children()
    }
}

@Composable
private fun ArticlesContent(
    state: LazyListState,
    id: ScreenId,
    query: Query,
    onMessage: (Message) -> Unit,
    children: LazyListScope.() -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = state,
        contentPadding = PaddingValues(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        item {
            ArticleSearchHeader(
                id = id,
                query = query,
                onMessage = onMessage
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        children()
    }
}

private fun LazyListScope.ArticlesContentNonEmptyImpl(
    id: ScreenId,
    query: Query,
    articles: List<Article>,
    onMessage: (Message) -> Unit,
    onLastElement: () -> Unit = { onMessage(LoadNextArticles(id)) },
) {
    require(articles.isNotEmpty())

    item {
        Text(
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start,
            text = query.toScreenTitle(),
            style = typography.subtitle1
        )

        Spacer(modifier = Modifier.height(16.dp))
    }

    itemsIndexed(articles, { _, item -> item.url }) { index, article ->
        Column {
            ArticleItem(
                screenId = id,
                article = article,
                onMessage = onMessage
            )

            if (index != articles.lastIndex) {
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (index == articles.lastIndex) {
                DisposableEffect(Unit) {
                    onLastElement()
                    onDispose { }
                }
            }
        }
    }
}

@Composable
private fun ArticleImage(
    imageUrl: URL?,
) {
    Surface(
        modifier = Modifier
            .height(200.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
        color = colors.onSurface.copy(alpha = 0.2f)
    ) {

        if (imageUrl != null) {
            Image(
                painter = rememberCoilPainter(
                    request = imageUrl.toExternalForm(),
                    fadeIn = true,
                ),
                contentDescription = "Article's Image",
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.Crop,
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterialApi::class)
private fun ArticleItem(
    screenId: ScreenId,
    article: Article,
    onMessage: (Message) -> Unit,
) {
    Card(
        elevation = 4.dp,
        shape = RoundedCornerShape(8.dp),
        onClick = { onMessage(NavigateToArticleDetails(article)) }
    ) {
        Column {

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
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Share"
            )
        }

        IconButton(
            onClick = { onMessage(ToggleArticleIsFavorite(screenId, article)) }
        ) {
            Icon(
                imageVector = if (article.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = if (article.isFavorite) "Remove from favorite" else "Add to favorite"
            )
        }
    }
}

@Composable
private fun ArticlesError(
    modifier: Modifier,
    id: ScreenId,
    message: String,
    onMessage: (Message) -> Unit,
) {
    Message(
        modifier,
        "Failed to load articles, message: '${message.replaceFirstChar { it.lowercase(Locale.getDefault()) }}'",
        "Retry"
    ) {
        onMessage(LoadArticlesFromScratch(id))
    }
}

@Composable
private fun Message(
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
            colors = textButtonColors(contentColor = colors.onSurface),
            onClick = onClick
        ) {
            Text(text = actionText)
        }
    }
}

@Composable
@OptIn(ExperimentalComposeUiApi::class)
private fun ArticleSearchHeader(
    modifier: Modifier = Modifier,
    id: ScreenId,
    query: Query,
    onMessage: (Message) -> Unit,
) {
    Card(
        modifier = modifier
            .statusBarsPadding()
            .fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {

        val keyboardController = LocalSoftwareKeyboardController.current

        TextField(
            value = query.input,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(text = query.type.toSearchHint(), style = typography.subtitle2) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            maxLines = 1,
            keyboardActions = KeyboardActions(
                onSearch = {
                    keyboardController?.hide()
                    onMessage(LoadArticlesFromScratch(id))
                }
            ),
            colors = TextFieldDefaults.textFieldColors(backgroundColor = colors.surface),
            textStyle = typography.subtitle2,
            trailingIcon = {
                IconButton(
                    onClick = {
                        keyboardController?.hide()
                        onMessage(LoadArticlesFromScratch(id))
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                }
            },
            onValueChange = { query -> onMessage(OnQueryUpdated(id, query)) }
        )
    }
}

@Composable
private fun listState(
    id: ScreenId,
    initialFirstVisibleItemIndex: Int = 0,
    initialFirstVisibleItemScrollOffset: Int = 0,
): LazyListState {

    val idToListState = remember { mutableMapOf<ScreenId, LazyListState>() }

    return remember(id) {
        idToListState.getOrPut(id) {
            LazyListState(
                initialFirstVisibleItemIndex,
                initialFirstVisibleItemScrollOffset,
            )
        }
    }
}

@Composable
@Render("Articles search input field")
private fun ArticleSearchHeaderPreview() {
    ThemedPreview {
        ArticleSearchHeader(
            id = UUID.randomUUID(),
            query = Query("some input text", Trending),
            onMessage = {}
        )
    }
}

@Composable
@Render("Article item")
private fun ArticleItemPreview() {
    ThemedPreview {
        ArticleItem(
            screenId = UUID.randomUUID(),
            article = ArticleSamplePreview,
            onMessage = {}
        )
    }
}

@Composable
@Render("Articles bottom action menu")
private fun ArticleActionsPreview() {
    ThemedPreview {
        ArticleActions(
            onMessage = {},
            article = ArticleSamplePreview,
            screenId = UUID.randomUUID()
        )
    }
}

@Composable
@Render("Messages preview")
private fun MessagePreview() {
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
@Render("Articles exception non empty list preview")
private fun ArticlesExceptionContentPreview() {
    ThemedPreview {
        ArticlesExceptionContent(
            state = rememberLazyListState(),
            id = UUID.randomUUID(),
            query = Query("Android", Regular),
            articles = listOf(ArticleSamplePreview),
            cause = RuntimeException("Some exception"),
            onMessage = { }
        )
    }
}

@Composable
@Render("Articles loading list preview")
private fun ArticlesLoadingContentPreview() {
    ThemedPreview {
        ArticlesLoadingContent(
            state = rememberLazyListState(),
            id = UUID.randomUUID(),
            query = Query("Android", Regular),
            articles = listOf(ArticleSamplePreview),
            onMessage = { }
        )
    }
}

private val DateFormatter: SimpleDateFormat by lazy {
    SimpleDateFormat("dd MMM' at 'hh:mm", Locale.getDefault())
}

private fun Throwable.toReadableMessage() =
    message?.replaceFirstChar { it.lowercase(Locale.getDefault()) } ?: "unknown exception"

private fun Query.toScreenTitle(): String =
    when (type) {
        Regular -> "Feed"
        Favorite -> "Favorite"
        Trending -> "Trending"
    }

private fun QueryType.toSearchHint(): String =
    when (this) {
        Regular -> "Search in articles"
        Favorite -> "Search in favorite"
        Trending -> "Search in trending"
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
