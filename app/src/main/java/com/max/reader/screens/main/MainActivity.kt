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

package com.max.reader.screens.main

import android.os.Bundle
import android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.*
import androidx.core.view.WindowCompat
import com.max.reader.BuildConfig.DEBUG
import com.max.reader.R
import com.max.reader.app.appComponent
import com.max.reader.app.closeAppCommands
import com.max.reader.misc.LocalLogCompositions
import com.max.reader.screens.article.details.ui.ArticleDetailsScreen
import com.max.reader.screens.home.HomeScreen
import com.max.reader.ui.theme.AppTheme
import com.oliynick.max.reader.app.*
import com.oliynick.max.reader.app.message.Message
import com.oliynick.max.reader.app.message.Pop
import com.oliynick.max.reader.app.navigation.currentTab
import com.oliynick.max.reader.article.details.ArticleDetailsState
import com.oliynick.max.reader.article.list.ArticlesState
import com.oliynick.max.reader.settings.SettingsState
import kotlinx.collections.immutable.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class MainActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_NewsReader)
        super.onCreate(savedInstanceState)
        // todo migrate at some point in the future
        @Suppress("DEPRECATION")
        window.setFlags(FLAG_TRANSLUCENT_STATUS, FLAG_TRANSLUCENT_STATUS)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val messages = remember { MutableSharedFlow<Message>() }
            val stateFlow = remember { appComponent(messages) }
            val appState = stateFlow.collectAsNullableState(context = Dispatchers.Main).value
                ?: return@setContent
            val scope = rememberCoroutineScope()
            val messageHandler = remember { scope.dispatcher(messages) }

            Application(appState, messageHandler)
        }

        launch {
            closeAppCommands.collect {
                finishAfterTransition()
            }
        }
    }

    override fun onDestroy() {
        cancel()
        super.onDestroy()
    }

    private fun CoroutineScope.dispatcher(
        messages: FlowCollector<Message>,
    ): (Message) -> Unit =
        { message -> launch { messages.emit(message) } }

}

@Composable
fun <T : R, R> Flow<T>.collectAsNullableState(
    context: CoroutineContext = EmptyCoroutineContext,
): State<R?> = collectAsState(context = context, initial = null)

@Composable
private fun Application(
    appState: AppState,
    onMessage: (Message) -> Unit,
) {
    AppTheme(
        isDarkModeEnabled = appState.isInDarkMode
    ) {

        BackHandler {
            onMessage(Pop)
        }

        CompositionLocalProvider(LocalLogCompositions provides DEBUG) {
            when (val screen = appState.screen) {
                is FullScreen -> FullScreen(screen, onMessage)
                is TabScreen -> TabScreen(appState, screen, onMessage)
                is NestedScreen -> TabScreen(appState, appState.currentTab, onMessage) {
                    TODO("Not implemented yet")
                }
            }
        }
    }
}

@Composable
private fun TabScreen(
    appState: AppState,
    screen: TabScreen,
    onMessage: (Message) -> Unit,
    content: (@Composable (innerPadding: PaddingValues) -> Unit)? = null
) {
    when (screen) {
        is ArticlesState -> HomeScreen(screen, onMessage, content)
        is SettingsState -> HomeScreen(appState, onMessage)
        else -> error("unhandled branch $screen")
    }
}

@Composable
private fun FullScreen(
    screen: FullScreen,
    onMessage: (Message) -> Unit,
) {
    when (screen) {
        is ArticleDetailsState -> ArticleDetailsScreen(screen, onMessage)
    }
}
