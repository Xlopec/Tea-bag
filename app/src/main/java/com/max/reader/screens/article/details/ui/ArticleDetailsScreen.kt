/*
 * Copyright (C) 2021. Maksym Oliinyk.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:Suppress("FunctionName")

package com.max.reader.screens.article.details.ui

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
import com.max.reader.app.Message
import com.max.reader.app.Pop
import com.max.reader.screens.article.details.ArticleDetailsState
import com.max.reader.screens.article.details.OpenInBrowser
import com.max.reader.ui.InsetAwareTopAppBar

@Composable
fun ArticleDetailsScreen(
    screen: ArticleDetailsState,
    onMessage: (Message) -> Unit,
) {
    val (canGoBack, updater) = remember(screen.id) { mutableStateOf(false) }
    val context = LocalContext.current
    val view = remember(screen.id) {
        AppWebView(context, updater)
            .apply { loadUrl(screen.article.url.toExternalForm()) }
    }

    Scaffold(
        topBar = {
            ArticleDetailsToolbar(
                canGoBack = canGoBack,
                screen = screen,
                onOpenInBrowser = { onMessage(OpenInBrowser(screen.id)) },
                onGoBack = {
                    if (canGoBack) {
                        view.goBack()
                    } else {
                        onMessage(Pop)
                    }
                })
        }, bodyContent = { innerPadding ->

            if (canGoBack) {
                BackHandler {
                    view.goBack()
                }
            }
            
            AndroidView(
                viewBlock = { view },
                modifier = Modifier.padding(innerPadding).fillMaxSize()
            )
        })
}

@Composable
private fun ArticleDetailsToolbar(
    canGoBack: Boolean,
    screen: ArticleDetailsState,
    onOpenInBrowser: () -> Unit,
    onGoBack: () -> Unit,
) {
    InsetAwareTopAppBar(
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
                text = screen.article.title.value
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
    updater: (Boolean) -> Unit,
) = WebView(context).apply {
    settings.javaScriptEnabled = true
    settings.setSupportZoom(true)
    webChromeClient = WebChromeClient()
    webViewClient = object : WebViewClient() {
        override fun doUpdateVisitedHistory(view: WebView, url: String?, isReload: Boolean) {
            if (!isReload) {
                updater(view.canGoBack())
            }
        }
    }
}
