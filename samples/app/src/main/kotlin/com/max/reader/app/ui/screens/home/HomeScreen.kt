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

@file:Suppress("FunctionName")

package com.max.reader.app.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons.Filled
import androidx.compose.material.icons.Icons.Outlined
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Language
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.max.reader.app.ui.misc.InsetAwareTopAppBar
import com.max.reader.app.ui.screens.article.ArticlesScreen
import com.max.reader.app.ui.screens.home.BottomMenuItem.Favorite
import com.max.reader.app.ui.screens.home.BottomMenuItem.Feed
import com.max.reader.app.ui.screens.home.BottomMenuItem.Settings
import com.max.reader.app.ui.screens.home.BottomMenuItem.Trending
import com.max.reader.app.ui.screens.settings.SettingsScreen
import com.oliynick.max.reader.app.AppState
import com.oliynick.max.reader.app.Message
import com.oliynick.max.reader.app.Navigation
import com.oliynick.max.reader.app.feature.article.list.ArticlesState
import com.oliynick.max.reader.app.feature.article.list.Query
import com.oliynick.max.reader.app.feature.article.list.RefreshArticles
import com.oliynick.max.reader.app.feature.navigation.NavigateToFavorite
import com.oliynick.max.reader.app.feature.navigation.NavigateToFeed
import com.oliynick.max.reader.app.feature.navigation.NavigateToSettings
import com.oliynick.max.reader.app.feature.navigation.NavigateToTrending
import com.oliynick.max.reader.app.feature.article.list.QueryType.Favorite as FavoriteQuery
import com.oliynick.max.reader.app.feature.article.list.QueryType.Regular as RegularQuery
import com.oliynick.max.reader.app.feature.article.list.QueryType.Trending as TrendingQuery

enum class BottomMenuItem {
    Feed,
    Favorite,
    Trending,
    Settings
}

@Composable
fun HomeScreen(
    state: ArticlesState,
    onMessage: (Message) -> Unit,
    content: (@Composable (innerPadding: PaddingValues) -> Unit)?
) {
    SwipeRefresh(
        state = rememberSwipeRefreshState(state.isRefreshing),
        swipeEnabled = state.isPreview,
        onRefresh = { onMessage(RefreshArticles(state.id)) },
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                BottomBar(
                    modifier = Modifier.navigationBarsPadding(),
                    item = state.query.toMenuItem(),
                    onMessage = onMessage
                )
            }, content = { innerPadding ->
                if (content == null) {
                    ArticlesScreen(state, onMessage, Modifier.padding(innerPadding))
                } else {
                    content(innerPadding)
                }
            })
    }
}

@Composable
fun HomeScreen(
    state: AppState,
    onMessage: (Message) -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            InsetAwareTopAppBar(
                backgroundColor = MaterialTheme.colors.surface,
                title = {
                    Text(text = "Settings")
                }
            )
        },
        bottomBar = {
            BottomBar(
                modifier = Modifier.navigationBarsPadding(),
                item = Settings,
                onMessage = onMessage
            )
        }, content = { innerPadding ->
            SettingsScreen(
                innerPadding = innerPadding,
                state = state,
                onMessage = onMessage
            )
        })
}

@Composable
operator fun PaddingValues.plus(other: PaddingValues) =
    PaddingValues(
        start = other.calculateStartPadding(LocalLayoutDirection.current),
        top = other.calculateTopPadding(),
        end = other.calculateEndPadding(LocalLayoutDirection.current),
        bottom = other.calculateBottomPadding()
    )

@Composable
fun BottomBar(
    modifier: Modifier,
    item: BottomMenuItem,
    onMessage: (Navigation) -> Unit,
) {
    BottomNavigation(modifier = modifier) {

        BottomNavigationItem(
            icon = {
                Icon(
                    imageVector = Outlined.Language,
                    contentDescription = "Feed"
                )
            },
            selected = item === Feed,
            onClick = { onMessage(NavigateToFeed) }
        )

        BottomNavigationItem(icon = {
            Icon(
                imageVector = Outlined.FavoriteBorder,
                contentDescription = "Favorite"
            )
        },
            selected = item === Favorite,
            onClick = { onMessage(NavigateToFavorite) }
        )

        BottomNavigationItem(icon = {
            Icon(
                imageVector = Filled.TrendingUp,
                contentDescription = "Trending"
            )
        },
            selected = item === Trending,
            onClick = { onMessage(NavigateToTrending) }
        )

        BottomNavigationItem(icon = {
            Icon(
                imageVector = Filled.Settings,
                contentDescription = "Settings"
            )
        },
            selected = item === Settings,
            onClick = { onMessage(NavigateToSettings) }
        )
    }
}

private fun Query.toMenuItem() = when (type) {
    RegularQuery -> Feed
    FavoriteQuery -> Favorite
    TrendingQuery -> Trending
}
