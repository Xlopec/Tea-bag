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

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.arkivanov.essenty.backhandler.BackDispatcher
import io.github.xlopec.reader.app.AppState
import io.github.xlopec.reader.app.FullScreen
import io.github.xlopec.reader.app.MessageHandler
import io.github.xlopec.reader.app.NestedScreen
import io.github.xlopec.reader.app.TabScreen
import io.github.xlopec.reader.app.feature.article.details.ArticleDetailsState
import io.github.xlopec.reader.app.feature.filter.FiltersState
import io.github.xlopec.reader.app.feature.navigation.Pop
import io.github.xlopec.reader.app.ui.screens.article.ArticleDetailsScreen
import io.github.xlopec.reader.app.ui.screens.filters.FiltersScreen
import io.github.xlopec.reader.app.ui.screens.home.HomeScreen
import io.github.xlopec.reader.app.ui.theme.AppTheme
import io.github.xlopec.tea.navigation.PredictiveBackContainer
import io.github.xlopec.tea.navigation.rememberDefaultPredictiveBackAnimation
import io.github.xlopec.tea.navigation.rememberPredictiveBackCoordinator
import kotlinx.coroutines.flow.Flow
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@Composable
internal fun App(
    app: AppState,
    handler: MessageHandler,
) {
    AppTheme(
        isDarkModeEnabled = app.settings.appDarkModeEnabled
    ) {
        val backDispatcher = remember { BackDispatcher() }

        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
        ) {
            val animation = rememberDefaultPredictiveBackAnimation(maxWidth = maxWidth)
            val coordinator = rememberPredictiveBackCoordinator(
                dispatcher = backDispatcher,
                stack = app.screens,
                animation = animation,
                onBackComplete = {
                    handler(Pop)
                },
            )

            PredictiveBackContainer(
                modifier = Modifier.fillMaxSize(),
                backDispatcher = backDispatcher,
                coordinator = coordinator,
            ) { modifier, screen ->
                when (screen) {
                    is FullScreen -> FullScreen(
                        modifier = modifier,
                        screen = screen,
                        handler = handler,
                    )

                    is TabScreen -> HomeScreen(
                        appState = app,
                        screen = screen,
                        onMessage = handler,
                        modifier = modifier,
                    )

                    is NestedScreen -> TODO()
                }
            }
        }
    }
}

@Composable
private fun FullScreen(
    modifier: Modifier,
    screen: FullScreen,
    handler: MessageHandler,
) {
    when (screen) {
        is ArticleDetailsState -> ArticleDetailsScreen(screen = screen, handler = handler, modifier = modifier)
        is FiltersState -> FiltersScreen(state = screen, handler = handler, modifier = modifier)
    }
}

@Composable
internal fun <T : R, R> Flow<T>.collectAsNullableState(
    context: CoroutineContext = EmptyCoroutineContext,
): State<R?> = collectAsState(context = context, initial = null)
