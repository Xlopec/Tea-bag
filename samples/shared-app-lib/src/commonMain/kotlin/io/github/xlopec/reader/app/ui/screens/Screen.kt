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

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Companion.End
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Companion.Start
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import io.github.xlopec.reader.app.AppState
import io.github.xlopec.reader.app.FullScreen
import io.github.xlopec.reader.app.MessageHandler
import io.github.xlopec.reader.app.Screen
import io.github.xlopec.reader.app.TabScreen
import io.github.xlopec.reader.app.feature.article.details.ArticleDetailsState
import io.github.xlopec.reader.app.feature.filter.FiltersState
import io.github.xlopec.reader.app.screen
import io.github.xlopec.reader.app.ui.screens.article.ArticleDetailsScreen
import io.github.xlopec.reader.app.ui.screens.filters.FiltersScreen
import io.github.xlopec.reader.app.ui.screens.home.HomeScreen
import io.github.xlopec.tea.core.Regular
import io.github.xlopec.tea.core.Snapshot
import kotlinx.coroutines.flow.Flow
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

private val NoTransition: ContentTransform = ContentTransform(
    targetContentEnter = EnterTransition.None,
    initialContentExit = ExitTransition.None,
    targetContentZIndex = 0f,
    sizeTransform = null
)

@Composable
internal fun ScreenTransition(
    modifier: Modifier,
    screen: Screen,
    snapshot: Snapshot<*, AppState, *>,
    handler: MessageHandler,
) {
    val currentScreen by rememberUpdatedState(screen)
    val currentState by rememberUpdatedState(snapshot.currentState)
    val previousScreen by rememberUpdatedState((snapshot as? Regular)?.previousState?.screen)
    val previousState by rememberUpdatedState((snapshot as? Regular)?.previousState)
    val transition = updateTransition(targetState = screen, label = "Screen transition")

    transition.AnimatedContent(
        transitionSpec = {
            screenTransition(currentScreen, previousScreen, currentState, previousState)
        },
        contentKey = { it.id.toString() }
    ) { animatedScreen ->
        Screen(
            modifier = modifier,
            screen = animatedScreen,
            app = currentState,
            handler = handler,
        )
    }
}

@Composable
internal fun Screen(
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

internal fun AnimatedContentTransitionScope<*>.screenTransition(
    currentScreen: Screen,
    previousScreen: Screen?,
    currentState: AppState,
    previousState: AppState?
): ContentTransform = when {
    skipTransition(currentScreen, previousScreen) -> NoTransition
    forwardNavigation(currentState, previousState) -> slideIntoContainer(Start) + fadeIn() togetherWith ExitTransition.None
    else -> slideIntoContainer(End) + fadeIn() togetherWith ExitTransition.None
}

private fun skipTransition(
    currentScreen: Screen,
    previousScreen: Screen?,
): Boolean {
    return currentScreen.id == previousScreen?.id ||
        (currentScreen is TabScreen && (previousScreen == null || tabChanged(previousScreen, currentScreen)))
}

private fun tabChanged(
    previousScreen: Screen,
    currentScreen: TabScreen,
): Boolean = previousScreen is TabScreen && previousScreen.tab != currentScreen.tab && currentScreen.id != previousScreen.id

private fun forwardNavigation(
    currentState: AppState,
    previousState: AppState?,
): Boolean {
    return currentState.screens.size > (previousState?.screens?.size ?: 0)
}
