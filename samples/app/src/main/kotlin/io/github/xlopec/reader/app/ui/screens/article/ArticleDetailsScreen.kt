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

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons.Default
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.viewinterop.AndroidView
import io.github.xlopec.reader.app.MessageHandler
import io.github.xlopec.reader.app.feature.article.details.ArticleDetailsState
import io.github.xlopec.reader.app.feature.article.details.OpenInBrowser
import io.github.xlopec.reader.app.feature.navigation.Pop
import io.github.xlopec.reader.app.ui.misc.ProgressInsetAwareTopAppBar

@Composable
fun ArticleDetailsScreen(
    screen: ArticleDetailsState,
    onMessage: MessageHandler,
) {
    val (canGoBack, backStackUpdater) = remember(screen.id) { mutableStateOf(false) }
    val (progress, loadProgressUpdater) = remember(screen.id) { mutableStateOf(0) }
    val (title, titleUpdater) = remember(screen.id) { mutableStateOf(screen.article.title.value) }
    val context = LocalContext.current
    val view = remember(screen.id) {
        AppWebView(context, titleUpdater, loadProgressUpdater, backStackUpdater)
            .apply { loadUrl(screen.article.url.toExternalForm()) }
    }

    Scaffold(
        topBar = {
            ArticleDetailsToolbar(
                canGoBack = canGoBack,
                loadProgress = progress,
                title = title,
                onOpenInBrowser = { onMessage(OpenInBrowser(screen.id)) },
                onGoBack = {
                    if (canGoBack) {
                        view.goBack()
                    } else {
                        onMessage(Pop)
                    }
                }
            )
        }, content = { innerPadding ->

            if (canGoBack) {
                BackHandler {
                    view.goBack()
                }
            }

            AndroidView(
                factory = { view },
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            )
        }
    )
}

@Composable
private fun ArticleDetailsToolbar(
    canGoBack: Boolean,
    title: String,
    loadProgress: Int,
    onOpenInBrowser: () -> Unit,
    onGoBack: () -> Unit,
) {
    ProgressInsetAwareTopAppBar(
        progress = loadProgress,
        modifier = Modifier.fillMaxWidth(),
        navigationIcon = {
            IconButton(onClick = onGoBack) {
                Icon(
                    contentDescription = if (canGoBack) "Back" else "Close",
                    imageVector = if (canGoBack) Default.ArrowBack else Default.Close,
                )
            }
        },
        title = {
            Text(
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                text = title
            )
        },
        actions = {
            IconButton(onClick = onOpenInBrowser) {
                Icon(
                    contentDescription = "Open in Browser",
                    imageVector = Default.OpenInBrowser
                )
            }
        }
    )
}

@SuppressLint("SetJavaScriptEnabled")
private fun AppWebView(
    context: Context,
    titleUpdater: (String) -> Unit,
    progressUpdater: (Int) -> Unit,
    backStackUpdater: (Boolean) -> Unit,
) = WebView(context).apply {
    settings.javaScriptEnabled = true
    settings.setSupportZoom(true)
    webChromeClient = object : WebChromeClient() {
        override fun onReceivedTitle(
            view: WebView,
            title: String,
        ) =
            titleUpdater(title)

        override fun onProgressChanged(
            view: WebView,
            newProgress: Int,
        ) =
            progressUpdater(newProgress)
    }
    webViewClient = object : WebViewClient() {
        override fun doUpdateVisitedHistory(
            view: WebView,
            url: String?,
            isReload: Boolean,
        ) {
            if (!isReload) {
                backStackUpdater(view.canGoBack())
            }
        }
    }
}
