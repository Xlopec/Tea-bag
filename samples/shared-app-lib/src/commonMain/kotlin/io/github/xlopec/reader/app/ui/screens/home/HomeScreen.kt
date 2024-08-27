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

package io.github.xlopec.reader.app.ui.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.Icons.Filled
import androidx.compose.material.icons.Icons.Outlined
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.xlopec.reader.app.AppState
import io.github.xlopec.reader.app.MessageHandler
import io.github.xlopec.reader.app.TabScreen
import io.github.xlopec.reader.app.feature.article.list.ArticlesState
import io.github.xlopec.reader.app.feature.article.list.RefreshArticles
import io.github.xlopec.reader.app.feature.article.list.ScrollState
import io.github.xlopec.reader.app.feature.article.list.SyncScrollPosition
import io.github.xlopec.reader.app.feature.navigation.NavigateToFavorite
import io.github.xlopec.reader.app.feature.navigation.NavigateToFeed
import io.github.xlopec.reader.app.feature.navigation.NavigateToSettings
import io.github.xlopec.reader.app.feature.navigation.NavigateToTrending
import io.github.xlopec.reader.app.feature.navigation.Navigation
import io.github.xlopec.reader.app.feature.settings.SettingsScreen
import io.github.xlopec.reader.app.misc.isException
import io.github.xlopec.reader.app.misc.isIdle
import io.github.xlopec.reader.app.misc.isRefreshing
import io.github.xlopec.reader.app.model.Filter
import io.github.xlopec.reader.app.ui.misc.InsetAwareTopAppBar
import io.github.xlopec.reader.app.ui.misc.InsetsAwareBottomNavigation
import io.github.xlopec.reader.app.ui.screens.article.Articles
import io.github.xlopec.reader.app.ui.screens.home.BottomMenuItem.Favorite
import io.github.xlopec.reader.app.ui.screens.home.BottomMenuItem.Feed
import io.github.xlopec.reader.app.ui.screens.home.BottomMenuItem.Settings
import io.github.xlopec.reader.app.ui.screens.home.BottomMenuItem.Trending
import io.github.xlopec.reader.app.ui.screens.settings.Settings
import io.github.xlopec.reader.app.model.FilterType.Favorite as FavoriteQuery
import io.github.xlopec.reader.app.model.FilterType.Regular as RegularQuery
import io.github.xlopec.reader.app.model.FilterType.Trending as TrendingQuery

internal enum class BottomMenuItem {
    Feed,
    Favorite,
    Trending,
    Settings
}

private typealias BottomBarContent = @Composable (BottomMenuItem) -> Unit
private typealias BottomBarListener = (reselected: Boolean, navigation: Navigation) -> Unit

private val LocalBottomBarListener = compositionLocalOf<BottomBarListener> { error("no bottom bar listener provided") }

@Composable
internal fun HomeScreen(
    appState: AppState,
    screen: TabScreen,
    onMessage: MessageHandler,
) {

    val bottomBar: BottomBarContent = remember {
        movableContentOf { item ->
            BottomBar(
                item = item,
            )
        }
    }

    when (screen) {
        is ArticlesState -> ArticlesScreen(
            state = screen,
            onMessage = onMessage,
            bottomBar = bottomBar
        )

        is SettingsScreen -> SettingsScreen(
            state = appState,
            onMessage = onMessage,
            bottomBar = bottomBar
        )

        else -> error("unhandled branch $screen")
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ArticlesScreen(
    state: ArticlesState,
    onMessage: MessageHandler,
    bottomBar: BottomBarContent,
) {
    val refreshState = rememberPullRefreshState(
        refreshing = state.loadable.isRefreshing,
        onRefresh = { onMessage(RefreshArticles(state.id)) },
    )

    Box(
        modifier = Modifier.pullRefresh(
            state = refreshState,
            enabled = state.isRefreshable
        ),
        contentAlignment = Alignment.TopCenter
    ) {
        val scrollTrigger = remember(state.id) { mutableIntStateOf(0) }
        val listState = remember(state.id) { state.scrollState.toLazyListState() }

        DisposableEffect(state.id) {
            onDispose {
                onMessage(SyncScrollPosition(state.id, listState.toScrollState()))
            }
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                CompositionLocalProvider(
                    LocalBottomBarListener provides { reselected, nav ->
                        onMessage(nav)

                        if (reselected) {
                            scrollTrigger.intValue++
                        }
                    }
                ) {
                    bottomBar(state.filter.toMenuItem())
                }

                if (scrollTrigger.intValue != 0) {
                    LaunchedEffect(scrollTrigger.intValue) {
                        listState.animateScrollToItem(0)
                    }
                }
            },
            content = { innerPadding ->
                Articles(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                    state = state,
                    listState = listState,
                    onMessage = onMessage
                )
            }
        )

        PullRefreshIndicator(
            modifier = Modifier.statusBarsPadding(),
            refreshing = state.loadable.isRefreshing,
            state = refreshState,
        )
    }
}

@Composable
private fun SettingsScreen(
    state: AppState,
    onMessage: MessageHandler,
    bottomBar: BottomBarContent,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            InsetAwareTopAppBar(
                title = {
                    Text(text = "Settings")
                }
            )
        },
        bottomBar = {
            CompositionLocalProvider(
                LocalBottomBarListener provides { _, nav ->
                    onMessage(nav)
                }
            ) {
                bottomBar(Settings)
            }
        },
        content = { innerPadding ->
            Settings(
                innerPadding = innerPadding,
                settings = state.settings,
                onMessage = onMessage
            )
        }
    )
}

@Composable
internal fun BottomBar(
    item: BottomMenuItem,
    modifier: Modifier = Modifier,
) {
    val handler = LocalBottomBarListener.current

    InsetsAwareBottomNavigation(
        modifier = modifier,
    ) {
        BottomNavigationItem(
            icon = {
                Icon(
                    imageVector = Outlined.Language,
                    contentDescription = "Feed"
                )
            },
            selected = item === Feed,
            onClick = { handler(item === Feed, NavigateToFeed) }
        )

        BottomNavigationItem(
            icon = {
                Icon(
                    imageVector = Outlined.FavoriteBorder,
                    contentDescription = "Favorite"
                )
            },
            selected = item === Favorite,
            onClick = { handler(item === Favorite, NavigateToFavorite) }
        )

        BottomNavigationItem(
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                    contentDescription = "Trending"
                )
            },
            selected = item === Trending,
            onClick = { handler(item === Trending, NavigateToTrending) }
        )

        BottomNavigationItem(
            icon = {
                Icon(
                    imageVector = Filled.Settings,
                    contentDescription = "Settings"
                )
            },
            selected = item === Settings,
            onClick = { handler(item === Settings, NavigateToSettings) }
        )
    }
}

private fun LazyListState.toScrollState() =
    ScrollState(firstVisibleItemIndex, firstVisibleItemScrollOffset)

private fun ScrollState.toLazyListState() =
    LazyListState(firstVisibleItemIndex, firstVisibleItemScrollOffset)

private fun Filter.toMenuItem() = when (type) {
    RegularQuery -> Feed
    FavoriteQuery -> Favorite
    TrendingQuery -> Trending
}

private val ArticlesState.isRefreshable: Boolean
    get() = loadable.data.isNotEmpty() && (loadable.isIdle || loadable.isException)
