@file:Suppress("FunctionName")

package com.max.reader.screens.home

import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.Icons.Default
import androidx.compose.material.icons.filled.Brightness3
import androidx.compose.material.icons.filled.Brightness5
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.max.reader.R
import com.max.reader.app.*
import com.max.reader.screens.article.list.ArticlesState
import com.max.reader.screens.article.list.Query
import com.max.reader.screens.article.list.QueryType
import com.max.reader.screens.article.list.RefreshArticles
import com.max.reader.screens.article.list.ui.ArticlesScreen
import com.max.reader.screens.home.BottomMenuItem.*
import com.max.reader.screens.settings.ToggleDarkMode
import com.max.reader.ui.InsetAwareTopAppBar
import com.max.reader.ui.SwipeToRefreshLayout
import dev.chrisbanes.accompanist.insets.navigationBarsPadding

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
    SwipeToRefreshLayout(
        refreshingState = state.isRefreshing,
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
            }, bodyContent = { innerPadding ->
                ArticlesScreen(Modifier.padding(innerPadding), state, onMessage)
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
        }, bodyContent = { innerPadding ->

            ScrollableColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {

                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {

                        Icon(imageVector = if (state.isDarkModeEnabled) Default.Brightness3 else Default.Brightness5)

                        Column {
                            Text(
                                text = "Dark mode",
                                style = typography.subtitle1
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "${if (state.isDarkModeEnabled) "Dis" else "En"}ables dark mode in the app",
                                style = typography.body1
                            )
                        }

                        Spacer(modifier = Modifier.weight(weight = 1f, fill = false))

                        Switch(
                            checked = state.isDarkModeEnabled,
                            onCheckedChange = { onMessage(ToggleDarkMode) }
                        )
                    }
                    Divider()
                }
            }
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
            Icon(imageVector = vectorResource(id = R.drawable.ic_language_white_24dp))
        },
            selected = item === Feed,
            onClick = { onMessage(NavigateToFeed) }
        )

        BottomNavigationItem(icon = {
            Icon(imageVector = vectorResource(id = R.drawable.ic_favorite_border_white_24dp))
        },
            selected = item === Favorite,
            onClick = { onMessage(NavigateToFavorite) }
        )

        BottomNavigationItem(icon = {
            Icon(imageVector = Icons.Filled.TrendingUp)
        },
            selected = item === Trending,
            onClick = { onMessage(NavigateToTrending) }
        )

        BottomNavigationItem(icon = {
            Icon(imageVector = Icons.Filled.Settings)
        },
            selected = item === Settings,
            onClick = { onMessage(NavigateToSettings) }
        )
    }
}

private fun Query.toMenuItem() = when (type) {
    QueryType.Regular -> Feed
    QueryType.Favorite -> Favorite
    QueryType.Trending -> Trending
}
