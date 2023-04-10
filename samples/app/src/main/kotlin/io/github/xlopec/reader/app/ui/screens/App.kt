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

package io.github.xlopec.reader.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import io.github.xlopec.reader.app.*
import io.github.xlopec.reader.app.feature.article.details.ArticleDetailsState
import io.github.xlopec.reader.app.feature.filter.FiltersState
import io.github.xlopec.reader.app.feature.navigation.Pop
import io.github.xlopec.reader.app.ui.misc.rememberWindowInsetsController
import io.github.xlopec.reader.app.ui.screens.article.ArticleDetailsScreen
import io.github.xlopec.reader.app.ui.screens.filters.FiltersScreen
import io.github.xlopec.reader.app.ui.screens.home.HomeScreen
import io.github.xlopec.reader.app.ui.theme.AppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@Composable
fun App(
    component: (Flow<Message>) -> Flow<AppState>,
) {
    val messages = remember { MutableSharedFlow<Message>() }
    val stateFlow = remember { component(messages) }
    val appState = stateFlow.collectAsNullableState(context = Dispatchers.Main).value ?: return
    val scope = rememberCoroutineScope { Dispatchers.Main.immediate }
    val messageHandler = remember { scope.messageHandler(messages) }

    App(
        appState = appState,
        onMessage = messageHandler
    )

    val systemUiController = rememberWindowInsetsController()

    LaunchedEffect(appState.settings.appDarkModeEnabled) {
        systemUiController.isAppearanceLightStatusBars = !appState.settings.appDarkModeEnabled
        systemUiController.isAppearanceLightNavigationBars = !appState.settings.appDarkModeEnabled
    }
}

@Composable
fun App(
    appState: AppState,
    onMessage: MessageHandler,
) {
    AppTheme(
        isDarkModeEnabled = appState.settings.appDarkModeEnabled
    ) {

        BackHandler {
            onMessage(Pop)
        }

        when (val screen = appState.screen) {
            is FullScreen -> FullScreen(
                screen = screen,
                onMessage = onMessage
            )
            is TabScreen -> HomeScreen(
                appState = appState,
                screen = screen,
                onMessage = onMessage
            )
            is NestedScreen -> TODO()
        }
    }
}

@Composable
private fun FullScreen(
    screen: FullScreen,
    onMessage: MessageHandler,
) {
    when (screen) {
        is ArticleDetailsState -> ArticleDetailsScreen(screen, onMessage)
        is FiltersState -> FiltersScreen(screen, onMessage)
    }
}

@Composable
private fun <T : R, R> Flow<T>.collectAsNullableState(
    context: CoroutineContext = EmptyCoroutineContext,
): State<R?> = collectAsState(context = context, initial = null)
