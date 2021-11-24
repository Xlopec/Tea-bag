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
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.google.accompanist.insets.statusBarsPadding
import com.max.reader.ui.theme.ThemedPreview
import com.oliynick.max.reader.app.Message
import com.oliynick.max.reader.app.NavigateToArticleDetails
import com.oliynick.max.reader.app.ScreenId
import com.oliynick.max.reader.article.list.*
import com.oliynick.max.reader.article.list.ArticlesState.TransientState.*
import com.oliynick.max.reader.article.list.QueryType.*
import com.oliynick.max.reader.domain.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.tooling.preview.Preview as Render

@Composable
fun ArticlesScreen(
    state: ArticlesState,
    onMessage: (Message) -> Unit,
    modifier: Modifier = Modifier,
) {

    val (id, _, articles, _, transientState) = state

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val listState = listState(id = id)
            .apply { setScrollingEnabled(articles.isNotEmpty(), rememberCoroutineScope()) }

        ArticlesContent(listState, state, onMessage) {

            if (articles.isNotEmpty()) {
                ArticleItems(state, onMessage)
            }

            TransientContent(id, articles.isEmpty(), transientState, onMessage)
        }
    }
}

private fun LazyListScope.ArticleItems(
    screen: ArticlesState,
    onMessage: (Message) -> Unit,
    onLastElement: () -> Unit = { onMessage(LoadNextArticles(screen.id)) },
) {
    val (id, query, articles) = screen

    require(articles.isNotEmpty()) { "Empty articles for screen=$screen" }

    item {
        Text(
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start,
            text = query.toScreenTitle(),
            style = typography.subtitle1
        )

        Spacer(modifier = Modifier.height(16.dp))
    }

    itemsIndexed(articles, { _, item -> item.url.toExternalForm() }) { index, article ->
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

private fun LazyListScope.TransientContent(
    id: ScreenId,
    isEmpty: Boolean,
    transientState: ArticlesState.TransientState,
    onMessage: (Message) -> Unit,
) = item {

    when (transientState) {
        is Exception ->
            ArticlesError(
                modifier = if (isEmpty) Modifier.fillParentMaxSize() else Modifier.fillParentMaxWidth(),
                id = id,
                message = transientState.th.readableMessage,
                onMessage = onMessage
            )
        is Loading -> ArticlesProgress(modifier = Modifier.fillParentMaxSize())
        is LoadingNext -> {
            Spacer(modifier = Modifier.height(16.dp))
            ArticlesProgress(modifier = Modifier.fillParentMaxWidth())
        }
        is Preview, is Refreshing -> {
            if (isEmpty) {
                Message(
                    modifier = Modifier.fillParentMaxSize(),
                    message = "No articles",
                    actionText = "Reload",
                    onClick = {
                        onMessage(LoadArticlesFromScratch(id))
                    }
                )
            }
        }
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
private fun ArticlesContent(
    listState: LazyListState,
    screen: ArticlesState,
    onMessage: (Message) -> Unit,
    children: LazyListScope.() -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        item {
            ArticleSearchHeader(state = screen, onMessage = onMessage)

            Spacer(modifier = Modifier.height(16.dp))
        }

        children()
    }
}

@Composable
private fun ArticleImage(
    imageUrl: Url?,
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
                painter = rememberImagePainter(
                    data = imageUrl.toExternalForm(),
                ) {
                    crossfade(true)
                },
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

        val author = article.author

        if (author != null) {
            Text(
                text = author.value,
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
            onClick = { onMessage(OnShareArticle(article)) }
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
    state: ArticlesState,
    onMessage: (Message) -> Unit,
) {
    val (id, query) = state

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
            state = ArticlesState(
                UUID.randomUUID(),
                Query("some input text", Trending),
                listOf(),
                false,
                Preview
            ),
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
            article = PreviewArticle,
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
            article = PreviewArticle,
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
@Render("Articles screen preview")
private fun ArticlesScreenPreview() {
    ThemedPreview {
        ArticlesScreen(
            state = ArticlesState(Trending, PreviewArticles, Preview),
            {}
        )
    }
}

@Composable
@Render("Articles screen loading next")
private fun ArticlesScreenLoadingNextPreview() {
    ThemedPreview {
        ArticlesScreen(
            state = ArticlesState(Trending, listOf(PreviewArticle), LoadingNext),
            {}
        )
    }
}

@Composable
@Render("Articles screen loading")
private fun ArticlesScreenLoadingPreview() {
    ThemedPreview {
        ArticlesScreen(
            state = ArticlesState(Trending, listOf(), Loading),
            {}
        )
    }
}

@Composable
@Render("Articles screen refreshing")
private fun ArticlesScreenRefreshingPreview() {
    ThemedPreview {
        ArticlesScreen(
            state = ArticlesState(Trending, listOf(PreviewArticle), Refreshing),
            {}
        )
    }
}

private fun ArticlesState(
    type: QueryType,
    articles: List<Article>,
    transientState: ArticlesState.TransientState
) = ArticlesState(UUID.randomUUID(), Query("input", type), articles, false, transientState)

private val DateFormatter: SimpleDateFormat by lazy {
    SimpleDateFormat("dd MMM' at 'hh:mm", Locale.getDefault())
}

private val Throwable.readableMessage: String
    get() = message?.replaceFirstChar { it.lowercase(Locale.getDefault()) } ?: "unknown exception"

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

private fun LazyListState.setScrollingEnabled(enabled: Boolean, scope: CoroutineScope) {
    scope.launch {
        scroll(scrollPriority = MutatePriority.PreventUserInput) {
            if (!enabled) {
                // Await indefinitely, blocking scrolls
                awaitCancellation()
            }
            // Do nothing, just cancel the previous indefinite "scroll"
        }
    }
}
