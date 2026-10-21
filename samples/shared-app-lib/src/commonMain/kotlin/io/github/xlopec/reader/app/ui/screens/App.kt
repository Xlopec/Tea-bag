/*
 * MIT License
 *
 * Copyright (c) 2026. Maksym Oliinyk.
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

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import io.github.xlopec.reader.app.AppState
import io.github.xlopec.reader.app.FullScreen
import io.github.xlopec.reader.app.MessageHandler
import io.github.xlopec.reader.app.Screen
import io.github.xlopec.reader.app.TabScreen
import io.github.xlopec.reader.app.feature.article.details.ArticleDetailsState
import io.github.xlopec.reader.app.feature.filter.FiltersState
import io.github.xlopec.reader.app.feature.navigation.Pop
import io.github.xlopec.reader.app.ui.screens.article.ArticleDetailsScreen
import io.github.xlopec.reader.app.ui.screens.filters.FiltersScreen
import io.github.xlopec.reader.app.ui.screens.home.HomeScreen
import io.github.xlopec.reader.app.ui.theme.AppTheme
import io.github.xlopec.tea.navigation.DefaultScreenTransition
import io.github.xlopec.tea.navigation.PredictiveBackContainer
import io.github.xlopec.tea.navigation.ScreenTransition
import io.github.xlopec.tea.navigation.StackEvent
import io.github.xlopec.tea.navigation.previousScreen
import io.github.xlopec.tea.navigation.screen
import kotlinx.coroutines.flow.Flow
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

// All tab screens render in one content slot, so a tab switch swaps the screen
// in place: the bottom bar (and its press ripple) survives instead of being
// torn down with the old slot. Anything per-tab inside HomeScreen must key on
// `screen.id` or it leaks across tabs.
private object TabsContentKey

@Composable
internal fun App(
    currentState: AppState,
    handler: MessageHandler,
) {
    AppTheme(
        isDarkModeEnabled = currentState.settings.appDarkModeEnabled,
    ) {
        PredictiveBackContainer(
            stack = currentState.screens,
            onBack = { handler(Pop) },
            modifier = Modifier.fillMaxSize(),
            backTarget = { stack ->
                // Filters runs its own in-place close animation triggered from the
                // close button / system back, so don't expose it to the gesture
                // overlay — the slide would compete with that animation.
                stack.previousScreen
                    ?.takeIf { stack.screen is FullScreen && stack.screen !is FiltersState }
            },
            contentKey = { screen -> if (screen is TabScreen) TabsContentKey else screen.id },
            // Filters expands out of / collapses back into the search bar itself;
            // the slide would double-animate it.
            transition = {
                when {
                    event == StackEvent.Push && to is FiltersState -> ScreenTransition.None
                    event == StackEvent.Pop && from is FiltersState -> ScreenTransition.None
                    else -> DefaultScreenTransition(event)
                }
            },
        ) { screen ->
            Screen(
                modifier = Modifier,
                screen = screen,
                app = currentState,
                handler = handler,
            )
        }
    }
}

@Composable
private fun Screen(
    modifier: Modifier,
    app: AppState,
    screen: Screen,
    handler: MessageHandler,
) {
    when (screen) {
        is FullScreen -> FullScreen(
            modifier = modifier,
            screen = screen,
            handler = handler,
        )

        is TabScreen -> HomeScreen(
            app = app,
            screen = screen,
            onMessage = handler,
            modifier = modifier,
        )
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
