@file:Suppress("FunctionName")

package com.max.reader.screens.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.vectorResource
import com.max.reader.R
import com.max.reader.app.*
import com.max.reader.screens.article.list.ArticlesState
import com.max.reader.screens.article.list.Query
import com.max.reader.screens.article.list.QueryType
import com.max.reader.screens.article.list.ui.ArticlesScreen
import com.max.reader.screens.home.BottomMenuItem.*
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

private fun Query.toMenuItem() = when (type) {
    QueryType.Regular -> Feed
    QueryType.Favorite -> Favorite
    QueryType.Trending -> Trending
}

@Composable
fun BottomBar(
    modifier: Modifier,
    item: BottomMenuItem,
    onMessage: (Navigation) -> Unit,
) {
    BottomNavigation(modifier = modifier) {

        BottomNavigationItem(icon = {
            Icon(asset = vectorResource(id = R.drawable.ic_language_white_24dp))
        },
            selected = item === Feed,
            onClick = { onMessage(NavigateToFeed) }
        )

        BottomNavigationItem(icon = {
            Icon(asset = vectorResource(id = R.drawable.ic_favorite_border_white_24dp))
        },
            selected = item === Favorite,
            onClick = { onMessage(NavigateToFavorite) }
        )

        BottomNavigationItem(icon = {
            Icon(asset = Icons.Filled.TrendingUp)
        },
            selected = item === Trending,
            onClick = { onMessage(NavigateToTrending) }
        )

        BottomNavigationItem(icon = {
            Icon(asset = Icons.Filled.Settings)
        },
            selected = item === Settings,
            onClick = { }
        )
    }
}
