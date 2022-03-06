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

package com.max.reader.app.ui.screens.article

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalTextInputService
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.max.reader.app.ui.misc.ColumnMessage
import com.max.reader.app.ui.misc.SearchHeader
import com.oliynick.max.entities.shared.Url
import com.oliynick.max.reader.app.AppException
import com.oliynick.max.reader.app.Message
import com.oliynick.max.reader.app.ScreenId
import com.oliynick.max.reader.app.domain.Article
import com.oliynick.max.reader.app.feature.*
import com.oliynick.max.reader.app.feature.article.list.*
import com.oliynick.max.reader.app.feature.article.list.QueryType.*
import com.oliynick.max.reader.app.feature.navigation.NavigateToArticleDetails
import com.oliynick.max.reader.app.feature.navigation.NavigateToSuggestions
import java.text.SimpleDateFormat
import java.util.*

internal const val ProgressIndicatorTag = "Progress Indicator"

@Composable
fun ArticlesScreen(
    state: ArticlesState,
    listState: LazyListState,
    modifier: Modifier = Modifier,
    onMessage: (Message) -> Unit,
) {

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        ArticlesContent(listState, state, onMessage) {

            if (state.hasDataToDisplay) {
                articleItems(state, onMessage)
            }

            transientContent(
                state.id,
                state.loadable.data.isEmpty(),
                state.loadable.loadableState,
                onMessage
            )
        }
    }
}

internal fun ArticleTestTag(
    url: Url
) = "Article $url"

private fun LazyListScope.articleItems(
    screen: ArticlesState,
    onMessage: (Message) -> Unit,
    onLastElement: () -> Unit = { onMessage(LoadNextArticles(screen.id)) },
) {

    val articles = screen.loadable.data

    require(articles.isNotEmpty()) { "Empty articles for screen=$screen" }

    item {
        Text(
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start,
            text = screen.query.toScreenTitle(),
            style = typography.subtitle1
        )

        Spacer(modifier = Modifier.height(16.dp))
    }

    itemsIndexed(articles, { _, item -> item.url.toExternalForm() }) { index, article ->
        Column(
            modifier = Modifier.semantics(mergeDescendants = true) {
                testTag = ArticleTestTag(article.url)
            }
        ) {
            ArticleItem(
                screenId = screen.id,
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

private fun LazyListScope.transientContent(
    id: ScreenId,
    isEmpty: Boolean,
    loadableState: LoadableState,
    onMessage: (Message) -> Unit,
) = item {

    when (loadableState) {
        is Exception ->
            ArticlesError(
                modifier = if (isEmpty) Modifier.fillParentMaxSize() else Modifier.fillParentMaxWidth(),
                id = id,
                message = loadableState.th.readableMessage,
                onMessage = onMessage
            )
        is Loading -> ArticlesProgress(modifier = Modifier.fillParentMaxSize())
        is LoadingNext -> {
            Spacer(modifier = Modifier.height(16.dp))
            ArticlesProgress(modifier = Modifier.fillParentMaxWidth())
        }
        is Preview, is Refreshing -> {
            if (isEmpty) {
                ColumnMessage(
                    modifier = Modifier.fillParentMaxSize().padding(16.dp),
                    message = "No articles"
                ) {
                    onMessage(LoadArticles(id))
                }
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
            modifier = Modifier.semantics { testTag = ProgressIndicatorTag },
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
        horizontalAlignment = Alignment.CenterHorizontally,
        userScrollEnabled = screen.loadable.data.isNotEmpty()
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
fun ArticleItem(
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
fun ArticleActions(
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
    ColumnMessage(
        modifier,
        "Failed to load articles, message: '${message.replaceFirstChar { it.lowercase(Locale.getDefault()) }}'"
    ) {
        onMessage(LoadArticles(id))
    }
}

@Composable
@OptIn(ExperimentalComposeUiApi::class)
fun ArticleSearchHeader(
    modifier: Modifier = Modifier,
    state: ArticlesState,
    onMessage: (Message) -> Unit,
) {

    val keyboardController = LocalSoftwareKeyboardController.current

    CompositionLocalProvider(
        LocalTextInputService provides null
    ) {
        var textFieldValue by remember { mutableStateOf(TextFieldValue(state.query.input)) }

        SearchHeader(
            inputText = textFieldValue,
            placeholderText = state.query.type.toSearchHint(),
            onQueryUpdate = { textFieldValue = it },
            onSearch = {
                keyboardController?.hide()
                onMessage(LoadArticles(state.id))
            },
            onFocusChanged = { focusState ->
                if (focusState.isFocused) {
                    onMessage(
                        NavigateToSuggestions(
                            state.id,
                            state.query,
                            textFieldValue.selection.start
                        )
                    )
                }
            }
        )
    }
}

private val DateFormatter: SimpleDateFormat by lazy {
    SimpleDateFormat("dd MMM' at 'hh:mm", Locale.getDefault())
}

private val AppException.readableMessage: String
    get() = message.replaceFirstChar { it.lowercase(Locale.getDefault()) }

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

private val ArticlesState.hasDataToDisplay: Boolean
    get() = loadable.data.isNotEmpty() && !loadable.isLoading
