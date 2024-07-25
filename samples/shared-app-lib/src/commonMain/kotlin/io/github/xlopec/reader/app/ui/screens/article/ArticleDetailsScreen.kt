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

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.Icons.Default
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.OpenInBrowser
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.multiplatform.webview.web.LoadingState
import com.multiplatform.webview.web.WebViewNavigator
import com.multiplatform.webview.web.rememberWebViewNavigator
import io.github.xlopec.reader.app.MessageHandler
import io.github.xlopec.reader.app.ScreenId
import io.github.xlopec.reader.app.feature.article.details.ArticleDetailsState
import io.github.xlopec.reader.app.feature.article.details.OpenInBrowser
import io.github.xlopec.reader.app.feature.article.details.ToggleArticleIsFavorite
import io.github.xlopec.reader.app.feature.article.list.OnShareArticle
import io.github.xlopec.reader.app.feature.navigation.Pop
import io.github.xlopec.reader.app.model.Article
import io.github.xlopec.reader.app.ui.misc.ProgressInsetAwareTopAppBar
import io.github.xlopec.reader.app.ui.screens.BackHandler
import io.github.xlopec.tea.data.Url
import io.github.xlopec.tea.data.toExternalValue

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun ArticleDetailsScreen(
    screen: ArticleDetailsState,
    onMessage: MessageHandler,
) {
    val webViewState = com.multiplatform.webview.web.rememberWebViewState(url = screen.article.url.toExternalValue())
    val navigator = rememberWebViewNavigator(rememberCoroutineScope())
    var isReloading by remember { mutableStateOf(false) }
    val refreshState = rememberPullRefreshState(
        refreshing = isReloading,
        onRefresh = {
            isReloading = true
            navigator.reload()
        },
    )

    LaunchedEffect(webViewState.loadingState) {
        when (webViewState.loadingState) {
            LoadingState.Finished -> isReloading = false
            LoadingState.Initializing -> isReloading = false
            is LoadingState.Loading -> {}
        }
    }

    Box(
        modifier = Modifier.pullRefresh(
            state = refreshState,
        ),
        contentAlignment = Alignment.TopCenter
    ) {

        Scaffold(
            content = { innerPadding ->
                if (navigator.canGoBack) {
                    BackHandler {
                        navigator.navigateBack()
                    }
                }
                /*CompositionLocalProvider(
                    LocalOverscrollConfiguration provides null
                ) {*/
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                ) {
                    //item {
                        ArticleDetailsToolbar(
                            id = screen.id,
                            state = webViewState,
                            article = screen.article,
                            navigator = navigator,
                            handler = onMessage
                        )
                  //  }
                  //  item {

                        com.multiplatform.webview.web.WebView(
                            modifier = Modifier.fillMaxSize(),
                            state = webViewState,
                            navigator = navigator,
                        )
                //    }
                }
                // }
            }
        )

        PullRefreshIndicator(
            modifier = Modifier.statusBarsPadding(),
            refreshing = isReloading,
            state = refreshState,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ArticleDetailsToolbar(
    id: ScreenId,
    navigator: WebViewNavigator,
    state: com.multiplatform.webview.web.WebViewState,
    article: Article,
    handler: MessageHandler,
) {
    val clipboardManager = LocalClipboardManager.current

    ProgressInsetAwareTopAppBar(
        progress = (state.loadingState as? LoadingState.Loading)?.progress?.let { it * 100 }?.toInt(),
        modifier = Modifier.fillMaxWidth(),
        navigationIcon = {
            IconButton(onClick = {
                if (navigator.canGoBack) {
                    navigator.navigateBack()
                } else {
                    handler(Pop)
                }
            }) {
                Icon(
                    contentDescription = if (navigator.canGoBack) "Back" else "Close",
                    imageVector = if (navigator.canGoBack) Icons.AutoMirrored.Filled.ArrowBack else Default.Close,
                )
            }
        },
        title = {
            Column {
                Text(
                    modifier = Modifier.basicMarquee(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    text = state.pageTitle ?: article.title.value
                )

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .combinedClickable(
                            onLongClick = {
                                clipboardManager.setText(AnnotatedString(article.url.toString()))
                            },
                            onLongClickLabel = "Copy to Clipboard",
                            onClick = { handler(OpenInBrowser(id)) }
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    val contentColor = LocalContentColor.current.copy(alpha = 0.68f)
                    val linkStyle = MaterialTheme.typography.caption

                    if (article.url.isHttps || article.url.isHttp) {
                        val iconSize = with(LocalDensity.current) {
                            if (linkStyle.fontSize.isSp) {
                                linkStyle.fontSize.toDp()
                            } else {
                                12.dp
                            }
                        }

                        Icon(
                            modifier = Modifier.size(iconSize),
                            imageVector = if (article.url.isHttps) Icons.Outlined.Lock else Icons.Outlined.LockOpen,
                            tint = contentColor,
                            contentDescription = null
                        )
                    }

                    Text(
                        text = article.url.toString(),
                        overflow = TextOverflow.Ellipsis,
                        style = linkStyle,
                        color = contentColor
                    )
                }
            }
        },
        actions = {
            var expanded by remember { mutableStateOf(false) }
            IconButton(onClick = { expanded = true }) {
                Icon(
                    contentDescription = null,
                    imageVector = Icons.Outlined.MoreVert
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        clipboardManager.setText(AnnotatedString(article.url.toString()))
                    },
                    imageVector = Icons.Outlined.ContentCopy,
                    text = "Copy URL"
                )

                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        handler(OnShareArticle(article))
                    },
                    imageVector = Default.Share,
                    text = "Share"
                )

                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        handler(ToggleArticleIsFavorite(id))
                    },
                    imageVector = if (article.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    text = if (article.isFavorite) "Remove from favorite" else "Add to favorite"
                )

                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        handler(OpenInBrowser(id))
                    },
                    imageVector = Icons.Outlined.OpenInBrowser,
                    text = "Open in default browser"
                )
            }
        }
    )
}

@Composable
private fun DropdownMenuItem(
    onClick: () -> Unit,
    imageVector: ImageVector,
    text: String,
) {
    DropdownMenuItem(onClick = onClick) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = null
            )

            Text(text = text)
        }
    }
}

private val Url.isHttps: Boolean
    get() = true// scheme == "https"

private val Url.isHttp: Boolean
    get() = false//scheme == "http"
