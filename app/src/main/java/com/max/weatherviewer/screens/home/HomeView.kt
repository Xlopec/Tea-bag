@file:Suppress("FunctionName")

package com.max.weatherviewer.screens.home

import androidx.compose.foundation.Text
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.vectorResource
import com.max.weatherviewer.R
import com.max.weatherviewer.app.*
import com.max.weatherviewer.screens.feed.Feed
import com.max.weatherviewer.screens.feed.LoadCriteria
import com.max.weatherviewer.screens.feed.ui.FeedScreen
import com.max.weatherviewer.screens.home.BottomMenuItem.*

@Composable
fun HomeScreen(
    screen: Feed,
    onMessage: (Message) -> Unit
) {

    Scaffold(topBar = {
        TopAppBar(title = {
            Text(text = screen.toScreenTitle())
        })
    }, bottomBar = {
        BottomBar(item = screen.criteria.toMenuItem(), onMessage = onMessage)
    }) {
        FeedScreen(screen, onMessage)
    }
}

enum class BottomMenuItem {
    FEED,
    FAVORITE,
    TRENDING
}

private fun LoadCriteria.toMenuItem() = when (this) {
    is LoadCriteria.Query -> FEED
    LoadCriteria.Favorite -> FAVORITE
    LoadCriteria.Trending -> TRENDING
}

@Composable
private fun BottomBar(
    item: BottomMenuItem,
    onMessage: (Navigation) -> Unit
) {
    BottomNavigation {

        BottomNavigationItem(icon = {
            Icon(asset = vectorResource(id = R.drawable.ic_language_white_24dp))
        },
            selected = item === FEED,
            onClick = { onMessage(NavigateToFeed) }
        )

        BottomNavigationItem(icon = {
            Icon(asset = vectorResource(id = R.drawable.ic_favorite_border_white_24dp))
        },
            selected = item === FAVORITE,
            onClick = { onMessage(NavigateToFavorite) }
        )

        BottomNavigationItem(icon = {
            Icon(asset = vectorResource(id = R.drawable.ic_trending_up_white_24dp))
        },
            selected = item === TRENDING,
            onClick = { onMessage(NavigateToTrending) }
        )
    }
}

private fun Feed.toScreenTitle(): String =
    when (criteria) {
        is LoadCriteria.Query -> "Feed"
        is LoadCriteria.Favorite -> "Favorite"
        is LoadCriteria.Trending -> "Trending"
    }
/*


import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import com.max.weatherviewer.R
import com.max.weatherviewer.app.*
import com.max.weatherviewer.screens.feed.Feed
import com.max.weatherviewer.screens.feed.LoadCriteria
import com.max.weatherviewer.screens.feed.ui.FeedScreen
import com.max.weatherviewer.ui.VectorImage
import com.max.weatherviewer.ui.VectorImageButton
import com.max.weatherviewer.ui.theme.lightThemeColors
import com.max.weatherviewer.ui.theme.themeTypography

@Composable
fun HomeScreen(
    screen: Feed,
    onMessage: (Message) -> Unit
) {

    HomeScreen(
        screen = screen,
        title = screen.toScreenTitle(),
        onMessage = onMessage,
        content = {
            FeedScreen(screen) { m -> onMessage(m) }
        },
        bottomBar = {
            val i = when (screen.criteria) {
                is LoadCriteria.Query -> 0
                LoadCriteria.Favorite -> 1
                LoadCriteria.Trending -> 2
            }

            BottomBar(i, onMessage)
        }
    )
}

@Composable
private fun HomeScreen(
    screen: Screen,
    title: String,
    onMessage: (Message) -> Unit,
    content: @Composable() () -> Unit,
    bottomBar: @Composable() (() -> Unit)? = null
) {
    MaterialTheme(
        colors = lightThemeColors,
        typography = themeTypography,
        children = {

            val (drawerState, onDrawerStateChange) = +state { DrawerState.Closed }

            ModalDrawerLayout(
                drawerState = drawerState,
                onStateChange = onDrawerStateChange,
                gesturesEnabled = drawerState == DrawerState.Opened,
                drawerContent = {
                    AppDrawer(
                        currentScreen = screen,
                        onMessage = onMessage,
                        closeDrawer = { onDrawerStateChange(DrawerState.Closed) }
                    )
                },
                bodyContent = {
                    Column {

                        TopAppBar(
                            title = { Text(text = title) },
                            navigationIcon = {
                                VectorImageButton(R.drawable.ic_menu_white_24dp) {
                                    onDrawerStateChange(DrawerState.Opened)
                                }
                            }
                        )

                        Container(modifier = Flexible(1f)) {
                            Crossfade(screen.id) {
                                content()
                            }
                        }

                        bottomBar?.invoke()
                    }
                }
            )
        }
    )
}

@Composable
private fun AppDrawer(
    currentScreen: Screen,
    onMessage: (Message) -> Unit,
    closeDrawer: () -> Unit
) {
    Column(
        modifier = Expanded
    ) {
        HeightSpacer(24.dp)
        Padding(16.dp) {
            Row {
                */
/*VectorImage(
                    id = R.drawable.ic_jetnews_logo,
                    tint = +themeColor { primary }
                )*//*

                WidthSpacer(8.dp)
                // VectorImage(R.drawable.ic_jetnews_wordmark)
            }
        }
        Divider(color = Color(0x14333333))
        DrawerButton(
            modifier = ExpandedWidth,
            icon = R.drawable.ic_home_24dp,
            label = "Home",
            isSelected = false
        ) {
            //navigateTo(Screen.Home)
            closeDrawer()
        }

        DrawerButton(
            icon = R.drawable.ic_arrow_back_24,
            label = "Interests",
            isSelected = true
        ) {
            // navigateTo(Screen.Interests)
            closeDrawer()
        }
    }
}

@Composable
private fun DrawerButton(
    modifier: Modifier = Modifier.None,
    @DrawableRes icon: Int,
    label: String,
    isSelected: Boolean,
    action: () -> Unit
) {
    val colors = +MaterialTheme.colors()
    val textIconColor = if (isSelected) {
        colors.primary
    } else {
        colors.onSurface.copy(alpha = 0.6f)
    }
    val backgroundColor = if (isSelected) {
        colors.primary.copy(alpha = 0.12f)
    } else {
        colors.surface
    }

    Surface(
        modifier = modifier wraps Spacing(left = 8.dp, top = 8.dp, right = 8.dp),
        color = backgroundColor,
        shape = RoundedCornerShape(4.dp)
    ) {
        Button(onClick = action, style = TextButtonStyle()) {
            Row {
                VectorImage(
                    modifier = Gravity.Center,
                    id = icon,
                    tint = textIconColor
                )
                WidthSpacer(16.dp)
                Text(
                    text = label,
                    style = (+MaterialTheme.typography()).body2.copy(
                        color = textIconColor
                    )
                )
            }
        }
    }
}

@Composable
private fun BottomBar(
    currentIndex: Int,
    onMessage: (Navigation) -> Unit
) {

    require(currentIndex in 0..2) { "Invalid index, was $currentIndex" }

    Surface(elevation = 2.dp) {
        Container(modifier = Height(56.dp) wraps Expanded) {
            FlexRow {

                val themeColors = +MaterialTheme.colors()
                val selected = themeColors.primary
                val nonSelected = themeColors.onSecondary

                expanded(1f) {
                    ImageButton(
                        id = R.drawable.ic_language_white_24dp,
                        tint = if (currentIndex == 0) selected else nonSelected
                    ) {
                        onMessage(NavigateToFeed)
                    }
                }
                expanded(1f) {
                    ImageButton(
                        id = R.drawable.ic_favorite_border_white_24dp,
                        tint = if (currentIndex == 1) selected else nonSelected
                    ) {
                        onMessage(NavigateToFavorite)
                    }
                }
                expanded(1f) {
                    ImageButton(
                        id = R.drawable.ic_trending_up_white_24dp,
                        tint = if (currentIndex == 2) selected else nonSelected
                    ) {
                        onMessage(NavigateToTrending)
                    }
                }
            }
        }
    }
}

@Composable
fun ImageButton(
    @DrawableRes id: Int,
    tint: Color = Color.Transparent,
    onClick: () -> Unit
) {
    Ripple(
        bounded = false,
        radius = 24.dp
    ) {
        Clickable(onClick = onClick) {
            Container(modifier = Spacing(12.dp) wraps Size(24.dp, 24.dp)) {
                DrawVector(
                    vectorImage = +vectorResource(id),
                    tintColor = tint
                )
            }
        }
    }
}

@Composable
fun BookmarkButton(
    isBookmarked: Boolean,
    onBookmark: (Boolean) -> Unit
) {
    Ripple(
        bounded = false,
        radius = 24.dp
    ) {
        Toggleable(isBookmarked, onBookmark) {
            Container(modifier = Size(48.dp, 48.dp)) {
                if (isBookmarked) {
                    VectorImage(id = R.drawable.ic_bookmark_white_24dp)
                } else {
                    VectorImage(id = R.drawable.ic_bookmark_border_white_24dp)
                }
            }
        }
    }
}


*/
