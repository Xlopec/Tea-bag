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

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
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
import io.github.xlopec.tea.navigation.PopTransitionSpec
import io.github.xlopec.tea.navigation.PredictiveBackContainer
import io.github.xlopec.tea.navigation.PredictivePopTransitionSpec
import io.github.xlopec.tea.navigation.PushTransitionSpec
import kotlinx.coroutines.flow.Flow
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

private val NoTransition: ContentTransform = ContentTransform(
    targetContentEnter = EnterTransition.None,
    initialContentExit = ExitTransition.None,
    targetContentZIndex = 0f,
    sizeTransform = null
)

private val PushTransition = PushTransitionSpec<Screen>()
private val PopTransition = PopTransitionSpec<Screen>()
private val PredictivePopTransition = PredictivePopTransitionSpec<Screen>()

/**
 * Shared App body — renders the navigation stack inside [AppTheme]. The platform
 * App composables own component subscription, snapshot collection, and any
 * platform-specific side effects (e.g. system bar appearance on Android).
 */
@Composable
internal fun App(
    currentState: AppState,
    handler: MessageHandler,
) {
    AppTheme(
        isDarkModeEnabled = currentState.settings.appDarkModeEnabled
    ) {
        PredictiveBackContainer(
            modifier = Modifier.fillMaxSize(),
            stack = currentState.screens,
            previousScreenFor = { stack, current ->
                // Filters runs its own in-place close animation triggered from the
                // close button / system back, so don't expose it to the gesture
                // overlay — the slide would compete with that animation.
                stack.getOrNull(stack.lastIndex - 1)
                    ?.takeIf { current is FullScreen && current !is FiltersState }
            },
            onBackComplete = { handler(Pop) },
            transitionSpec = { screenTransition() },
            popTransitionSpec = { popScreenTransition() },
            predictivePopTransitionSpec = PredictivePopTransition,
            endEdgeEnabled = false,
        ) { screen ->
            Screen(
                modifier = Modifier,
                screen = screen,
                app = currentState,
                handler = handler
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

/**
 * Forward (push) transition spec for the app. PredictiveBackContainer
 * routes pops to its own pop spec, so this only fires for non-pop changes —
 * forward navigation or tab swaps. Filters runs its own open animation
 * (expanding out of the search bar) so the iOS push slide would double up;
 * tab swaps and same-screen rebinds also skip. Everything else uses iOS push.
 */
private fun AnimatedContentTransitionScope<Screen>.screenTransition(): ContentTransform =
    when {
        targetState is FiltersState -> NoTransition
        skipTransition(targetState, initialState) -> NoTransition
        else -> PushTransition(this)
    }

/**
 * Programmatic (non-gesture) pop transition spec. Filters runs its own
 * in-place collapse back into the Articles search bar before dispatching
 * Pop, so layering the iOS slide on top would double-animate; skip the
 * pop slide in that case.
 */
private fun AnimatedContentTransitionScope<Screen>.popScreenTransition(): ContentTransform =
    when (initialState) {
        is FiltersState -> NoTransition
        else -> PopTransition(this)
    }

private fun skipTransition(
    currentScreen: Screen,
    previousScreen: Screen?,
): Boolean =
    currentScreen.id == previousScreen?.id ||
        (currentScreen is TabScreen && (previousScreen == null || tabChanged(previousScreen, currentScreen)))

private fun tabChanged(
    previousScreen: Screen,
    currentScreen: TabScreen,
): Boolean = previousScreen is TabScreen && previousScreen.tab != currentScreen.tab && currentScreen.id != previousScreen.id
