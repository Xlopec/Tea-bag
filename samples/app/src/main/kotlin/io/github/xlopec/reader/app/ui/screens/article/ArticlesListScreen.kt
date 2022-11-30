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

@file:Suppress("FunctionName")

package io.github.xlopec.reader.app.ui.screens.article

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalTextInputService
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import io.github.xlopec.reader.app.AppException
import io.github.xlopec.reader.app.MessageHandler
import io.github.xlopec.reader.app.ScreenId
import io.github.xlopec.reader.app.feature.article.list.*
import io.github.xlopec.reader.app.feature.navigation.NavigateToArticleDetails
import io.github.xlopec.reader.app.feature.navigation.NavigateToFilters
import io.github.xlopec.reader.app.misc.*
import io.github.xlopec.reader.app.model.Article
import io.github.xlopec.reader.app.model.Filter
import io.github.xlopec.reader.app.model.FilterType
import io.github.xlopec.reader.app.model.FilterType.*
import io.github.xlopec.reader.app.ui.misc.ColumnMessage
import io.github.xlopec.reader.app.ui.misc.SearchHeader
import io.github.xlopec.tea.data.Url
import io.github.xlopec.tea.data.toExternalValue
import java.text.SimpleDateFormat
import java.util.*

internal const val ProgressIndicatorTag = "Progress Indicator"

@Composable
fun ArticlesScreen(
    state: ArticlesState,
    listState: LazyListState,
    modifier: Modifier = Modifier,
    onMessage: MessageHandler,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        ArticlesContent(listState, state, onMessage) {

            if (state.hasDataToDisplay) {
                articleItems(state, onMessage)
            }

            loadableContent(
                state.id,
                state.loadable.data.isEmpty(),
                state.loadable.loadableState,
                state.filter.type,
                onMessage
            )
        }
    }
}

internal fun ArticleTestTag(
    url: Url,
) = "Article $url"

private fun LazyListScope.articleItems(
    screen: ArticlesState,
    onMessage: MessageHandler,
) {
    val articles = screen.loadable.data

    require(articles.isNotEmpty()) { "Empty articles for screen=$screen" }

    val title = screen.filter.toScreenTitle()

    item(
        key = title,
        contentType = screen.filter::class
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start,
            text = title,
            style = typography.subtitle1
        )
    }

    itemsIndexed(
        items = articles,
        key = { _, item -> item.url.toString() },
        contentType = { _, item -> item::class }
    ) { index, article ->
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

            if (index == articles.lastIndex) {
                LaunchedEffect(Unit) {
                    onMessage(LoadNextArticles(screen.id))
                }
            }
        }
    }
}

private fun LazyListScope.loadableContent(
    id: ScreenId,
    isEmpty: Boolean,
    loadableState: LoadableState,
    filterType: FilterType,
    onMessage: MessageHandler,
) = item(
    key = loadableState::class.simpleName,
    contentType = loadableState::class
) {
    when (loadableState) {
        is Exception ->
            ArticlesError(
                modifier = if (isEmpty) Modifier.fillParentMaxSize() else Modifier.fillParentMaxWidth(),
                message = loadableState.th.readableMessage,
                onRetry = { onMessage(if (isEmpty) LoadArticles(id) else LoadNextArticles(id)) }
            )

        is Loading -> ArticlesProgress(modifier = Modifier.fillParentMaxSize())
        is LoadingNext -> ArticlesProgress(modifier = Modifier.fillParentMaxWidth())
        is Idle, is Refreshing -> {
            if (isEmpty) {
                ColumnMessage(
                    modifier = Modifier
                        .fillParentMaxSize()
                        .padding(16.dp),
                    title = "No articles",
                    message = filterType.toEmptyStateDescription(),
                    onClick = { onMessage(LoadArticles(id)) }
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
            modifier = Modifier.semantics { testTag = ProgressIndicatorTag },
            color = colors.secondaryVariant
        )
    }
}

@Composable
private fun ArticlesContent(
    listState: LazyListState,
    screen: ArticlesState,
    onMessage: MessageHandler,
    children: LazyListScope.() -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        userScrollEnabled = screen.loadable.data.isNotEmpty()
    ) {

        item(
            key = "header",
            contentType = "header"
        ) { ArticleSearchHeader(state = screen, onMessage = onMessage) }

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
            AsyncImage(
                model = imageUrl.toImageRequest(),
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
    onMessage: MessageHandler,
) {
    Card(
        elevation = CardElevation,
        shape = CardShape,
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
    Column(
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {

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
    onMessage: MessageHandler,
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
    message: String,
    onRetry: () -> Unit,
) {
    ColumnMessage(
        modifier = modifier,
        title = "Oops, something went wrong",
        message = "Failed to load articles, message: '${message.toDisplayErrorMessage()}'",
        onClick = onRetry
    )
}

@Composable
@OptIn(ExperimentalComposeUiApi::class)
fun ArticleSearchHeader(
    state: ArticlesState,
    onMessage: MessageHandler,
) {

    val keyboardController = LocalSoftwareKeyboardController.current

    CompositionLocalProvider(
        LocalTextInputService provides null
    ) {
        SearchHeader(
            inputText = state.filter.query?.value ?: "",
            placeholderText = state.filter.type.toSearchHint(),
            onQueryUpdate = { },
            onSearch = {
                keyboardController?.hide()
                onMessage(LoadArticles(state.id))
            },
            onFocusChanged = { focusState ->
                if (focusState.isFocused) {
                    onMessage(
                        NavigateToFilters(
                            state.id,
                            state.filter,
                        )
                    )
                }
            }
        )
    }
}

private val CardElevation = 4.dp
private val CardShape = RoundedCornerShape(8.dp)

private val DateFormatter: SimpleDateFormat by lazy {
    SimpleDateFormat("dd MMM' at 'hh:mm", Locale.getDefault())
}

private val AppException.readableMessage: String
    get() = message.replaceFirstChar { it.lowercase(Locale.getDefault()) }

private fun Filter.toScreenTitle(): String =
    when (type) {
        Regular -> "Feed"
        Favorite -> "Favorite"
        Trending -> "Trending"
    }

fun FilterType.toSearchHint(): String =
    when (this) {
        Regular -> "Search in articles"
        Favorite -> "Search in favorite"
        Trending -> "Search in trending"
    }

private fun FilterType.toEmptyStateDescription() =
    when (this) {
        Regular, Trending -> "There are no articles matching search criteria"
        Favorite -> "There are no favorite articles. Let's add some"
    }

@Composable
private fun Url.toImageRequest() = ImageRequest.Builder(LocalContext.current)
    .data(toExternalValue())
    .crossfade(true)
    .build()

private val ArticlesState.hasDataToDisplay: Boolean
    get() = loadable.data.isNotEmpty() && !loadable.isLoading

private fun String.toDisplayErrorMessage() = replaceFirstChar { it.lowercase(Locale.getDefault()) }
