package com.max.weatherviewer.presentation.main

import androidx.compose.Composable
import androidx.ui.material.MaterialTheme
import com.max.weatherviewer.Home
import com.max.weatherviewer.Message
import com.max.weatherviewer.Screen
import com.max.weatherviewer.presentation.HomeScreen
import com.max.weatherviewer.presentation.theme.lightThemeColors
import com.max.weatherviewer.presentation.theme.themeTypography
import com.max.weatherviewer.safe

@Composable
fun App(children: @Composable() () -> Unit) {
    MaterialTheme(
        colors = lightThemeColors,
        typography = themeTypography,
        children = children
    )
}

@Composable
fun Screen(screen: Screen, onMessage: (Message) -> Unit) {
    when(screen) {
        is Home -> HomeScreen(screen, onMessage)
    }.safe
}

