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

import android.content.Context
import android.graphics.Bitmap
import android.view.ViewGroup.LayoutParams
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
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
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.widget.NestedScrollView
import io.github.xlopec.reader.app.MessageHandler
import io.github.xlopec.reader.app.ScreenId
import io.github.xlopec.reader.app.feature.article.details.ArticleDetailsState
import io.github.xlopec.reader.app.feature.article.details.OpenInBrowser
import io.github.xlopec.reader.app.feature.article.details.ToggleArticleIsFavorite
import io.github.xlopec.reader.app.feature.article.list.OnShareArticle
import io.github.xlopec.reader.app.feature.navigation.Pop
import io.github.xlopec.reader.app.model.Article
import io.github.xlopec.reader.app.ui.misc.ProgressInsetAwareTopAppBar
import io.github.xlopec.tea.data.Url

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun ArticleDetailsScreen(
    screen: ArticleDetailsState,
    onMessage: MessageHandler,
) {
    val webViewState = rememberWebViewState(screen.article.url)
    val refreshState = rememberPullRefreshState(
        refreshing = webViewState.isReloading,
        onRefresh = webViewState::reload,
    )

    Box(
        modifier = Modifier.pullRefresh(
            state = refreshState,
        ),
        contentAlignment = Alignment.TopCenter
    ) {

        Scaffold(
            content = { innerPadding ->
                if (webViewState.canGoBack) {
                    BackHandler {
                        webViewState.navigateBack()
                    }
                }
                CompositionLocalProvider(
                    LocalOverscrollConfiguration provides null
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                    ) {
                        item {
                            ArticleDetailsToolbar(
                                id = screen.id,
                                state = webViewState,
                                article = screen.article,
                                handler = onMessage
                            )
                        }
                        item {
                            AndroidView(
                                modifier = Modifier.fillMaxSize(),
                                factory = {
                                    NestedScrollView(it).apply {
                                        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                                        addView(webViewState.webView, 0, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
                                    }
                                },
                            )
                        }
                    }
                }
            }
        )

        PullRefreshIndicator(
            modifier = Modifier.statusBarsPadding(),
            refreshing = webViewState.isReloading,
            state = refreshState,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ArticleDetailsToolbar(
    id: ScreenId,
    state: WebViewState,
    article: Article,
    handler: MessageHandler,
) {
    val clipboardManager = LocalClipboardManager.current

    ProgressInsetAwareTopAppBar(
        progress = state.loadProgress,
        modifier = Modifier.fillMaxWidth(),
        navigationIcon = {
            IconButton(onClick = {
                if (state.canGoBack) {
                    state.navigateBack()
                } else {
                    handler(Pop)
                }
            }) {
                Icon(
                    contentDescription = if (state.canGoBack) "Back" else "Close",
                    imageVector = if (state.canGoBack) Icons.AutoMirrored.Filled.ArrowBack else Default.Close,
                )
            }
        },
        title = {
            Column {
                Text(
                    modifier = Modifier.basicMarquee(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    text = state.title ?: article.title.value
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

@Composable
private fun rememberWebViewState(
    url: Url,
    javascriptEnabled: Boolean = true,
    zoomSupportEnabled: Boolean = true,
): WebViewState {
    val context = LocalContext.current
    val state = remember {
        WebViewState(
            url = url,
            context = context,
            javascriptEnabled = javascriptEnabled,
            zoomSupportEnabled = zoomSupportEnabled
        )
    }

    DisposableEffect(Unit) {
        onDispose(state.webView::destroy)
    }

    return state
}

@Stable
private class WebViewState(
    url: Url,
    context: Context,
    javascriptEnabled: Boolean = true,
    zoomSupportEnabled: Boolean = true,
) {
    var title by mutableStateOf<String?>(null)
        private set

    var canGoBack by mutableStateOf(false)
        private set

    var loadProgress by mutableIntStateOf(0)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var isReloading by mutableStateOf(false)
        private set

    val webView = WebView(context).apply {
        settings.javaScriptEnabled = javascriptEnabled
        settings.setSupportZoom(zoomSupportEnabled)
        webChromeClient = object : WebChromeClient() {
            override fun onReceivedTitle(
                view: WebView,
                title: String,
            ) {
                this@WebViewState.title = title
            }

            override fun onProgressChanged(
                view: WebView,
                newProgress: Int,
            ) {
                loadProgress = newProgress
            }
        }
        webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                isLoading = true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                isLoading = false
                isReloading = false
            }

            override fun doUpdateVisitedHistory(
                view: WebView,
                url: String?,
                isReload: Boolean,
            ) {
                if (!isReload) {
                    canGoBack = view.canGoBack()
                }
            }
        }

        loadUrl(url.toString())
    }

    fun reload() {
        isReloading = true
        webView.reload()
    }

    fun navigateBack() = webView.goBack()
}

private val Url.isHttps: Boolean
    get() = scheme == "https"

private val Url.isHttp: Boolean
    get() = scheme == "http"
