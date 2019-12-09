@file:Suppress("FunctionName")

package com.max.weatherviewer.presentation.main

import androidx.annotation.DrawableRes
import androidx.compose.Composable
import androidx.compose.state
import androidx.compose.unaryPlus
import androidx.ui.core.Modifier
import androidx.ui.core.Text
import androidx.ui.core.dp
import androidx.ui.foundation.Clickable
import androidx.ui.foundation.selection.Toggleable
import androidx.ui.foundation.shape.corner.RoundedCornerShape
import androidx.ui.graphics.Color
import androidx.ui.graphics.vector.DrawVector
import androidx.ui.layout.*
import androidx.ui.material.*
import androidx.ui.material.ripple.Ripple
import androidx.ui.material.surface.Surface
import androidx.ui.res.vectorResource
import com.max.weatherviewer.R
import com.max.weatherviewer.app.*
import com.max.weatherviewer.home.Feed
import com.max.weatherviewer.home.LoadCriteria
import com.max.weatherviewer.presentation.FeedScreen
import com.max.weatherviewer.presentation.VectorImage
import com.max.weatherviewer.presentation.VectorImageButton
import com.max.weatherviewer.presentation.theme.lightThemeColors
import com.max.weatherviewer.presentation.theme.themeTypography
import com.max.weatherviewer.safe

@Composable
fun App(
    screen: Screen,
    onMessage: (Message) -> Unit,
    children: @Composable() () -> Unit
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
                            title = { Text(text = "News Reader") },
                            navigationIcon = {
                                VectorImageButton(R.drawable.ic_menu_white_24dp) {
                                    onDrawerStateChange(DrawerState.Opened)
                                }
                            }
                        )

                        Container(modifier = Flexible(1f)) {
                            children()
                        }

                        BottomBar(screen as Feed, onMessage)
                    }
                }
            )
        }
    )
}

@Composable
fun Screen(screen: Screen, onMessage: (Message) -> Unit) {
    when (screen) {
        is Feed -> FeedScreen(screen, onMessage)
        else -> TODO()
    }.safe
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
                /*VectorImage(
                    id = R.drawable.ic_jetnews_logo,
                    tint = +themeColor { primary }
                )*/
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
    current: Feed,
    onMessage: (Navigation) -> Unit
) {

    Surface(elevation = 2.dp) {
        Container(modifier = Height(56.dp) wraps Expanded) {
            FlexRow {
                expanded(1f) {
                    BottomBarAction(if (current.criteria is LoadCriteria.Query) R.drawable.ic_language_red_24dp else R.drawable.ic_language_white_24dp) {
                        onMessage(NavigateToFeed)
                    }
                }
                expanded(1f) {
                    BottomBarAction(if (current.criteria is LoadCriteria.Favorite) R.drawable.ic_favorite_border_red_24dp else R.drawable.ic_favorite_border_white_24dp) {
                        onMessage(NavigateToFavorite)
                    }
                }
                expanded(1f) {
                    BottomBarAction(if (current.criteria is LoadCriteria.Trending) R.drawable.ic_trending_up_red_24dp else R.drawable.ic_trending_up_white_24dp) {
                        onMessage(NavigateToTranding)
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomBarAction(
    @DrawableRes id: Int,
    onClick: () -> Unit
) {
    Ripple(
        bounded = false,
        radius = 24.dp
    ) {
        Clickable(onClick = onClick) {
            Container(modifier = Spacing(12.dp) wraps Size(24.dp, 24.dp)) {
                DrawVector(+vectorResource(id))
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
