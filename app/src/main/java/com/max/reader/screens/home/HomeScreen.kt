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

package com.max.reader.screens.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons.Filled
import androidx.compose.material.icons.Icons.Outlined
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Language
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.max.reader.screens.article.list.ui.ArticlesScreen
import com.max.reader.screens.home.BottomMenuItem.Favorite
import com.max.reader.screens.home.BottomMenuItem.Feed
import com.max.reader.screens.home.BottomMenuItem.Settings
import com.max.reader.screens.home.BottomMenuItem.Trending
import com.max.reader.screens.settings.SettingsScreen
import com.max.reader.ui.InsetAwareTopAppBar
import com.oliynick.max.reader.app.*
import com.oliynick.max.reader.article.list.ArticlesState
import com.oliynick.max.reader.article.list.Query
import com.oliynick.max.reader.article.list.RefreshArticles
import com.oliynick.max.reader.article.list.QueryType.Favorite as FavoriteQuery
import com.oliynick.max.reader.article.list.QueryType.Regular as RegularQuery
import com.oliynick.max.reader.article.list.QueryType.Trending as TrendingQuery

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
                ArticlesScreen(state, onMessage, Modifier.padding(innerPadding))
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
fun BottomBar(
    modifier: Modifier,
    item: BottomMenuItem,
    onMessage: (Navigation) -> Unit,
) {
    BottomNavigation(modifier = modifier) {

        BottomNavigationItem(icon = {
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
