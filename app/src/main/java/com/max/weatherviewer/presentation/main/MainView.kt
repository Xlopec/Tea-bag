package com.max.weatherviewer.presentation.main

import androidx.annotation.DrawableRes
import androidx.compose.Composable
import androidx.compose.state
import androidx.compose.unaryPlus
import androidx.ui.core.Text
import androidx.ui.core.dp
import androidx.ui.foundation.shape.corner.RoundedCornerShape
import androidx.ui.graphics.Color
import androidx.ui.layout.*
import androidx.ui.material.*
import androidx.ui.material.surface.Surface
import com.max.weatherviewer.*
import com.max.weatherviewer.R
import com.max.weatherviewer.app.Home
import com.max.weatherviewer.app.Message
import com.max.weatherviewer.app.Screen
import com.max.weatherviewer.presentation.HomeScreen
import com.max.weatherviewer.presentation.VectorImage
import com.max.weatherviewer.presentation.VectorImageButton
import com.max.weatherviewer.presentation.theme.lightThemeColors
import com.max.weatherviewer.presentation.theme.themeTypography

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
                    FlexColumn {
                        inflexible {
                            TopAppBar(
                                title = { Text(text = "News Reader") },
                                navigationIcon = {
                                    VectorImageButton(R.drawable.ic_menu_white_24dp) {
                                        onDrawerStateChange(DrawerState.Opened)
                                    }
                                }
                            )
                        }
                        flexible(flex = 1f) {
                            children()
                        }
                    }
                }
            )
        }
    )
}

@Composable
fun Screen(screen: Screen, onMessage: (Message) -> Unit) {
    when (screen) {
        is Home -> HomeScreen(screen, onMessage)
    }.safe
}

@Composable
private fun AppDrawer(
    currentScreen: Screen,
    onMessage: (Message) -> Unit,
    closeDrawer: () -> Unit
) {
    Column(
        crossAxisSize = LayoutSize.Expand,
        mainAxisSize = LayoutSize.Expand
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
    @DrawableRes icon: Int,
    label: String,
    isSelected: Boolean,
    action: () -> Unit
) {
    val textIconColor = if (isSelected) {
        +themeColor { primary }
    } else {
        (+themeColor { onSurface }).copy(alpha = 0.6f)
    }
    val backgroundColor = if (isSelected) {
        (+themeColor { primary }).copy(alpha = 0.12f)
    } else {
        +themeColor { surface }
    }

    Padding(left = 8.dp, top = 8.dp, right = 8.dp) {
        Surface(
            color = backgroundColor,
            shape = RoundedCornerShape(4.dp)
        ) {
            Button(onClick = action, style = TextButtonStyle()) {
                Row(
                    mainAxisSize = LayoutSize.Expand,
                    crossAxisAlignment = CrossAxisAlignment.Center
                ) {
                    VectorImage(
                        id = icon,
                        tint = textIconColor
                    )
                    WidthSpacer(16.dp)
                    Text(
                        text = label,
                        style = (+themeTextStyle { body2 }).copy(
                            color = textIconColor
                        )
                    )
                }
            }
        }
    }
}
