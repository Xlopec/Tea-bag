package com.max.reader.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable

@Composable
fun AppTheme(
     content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = AppDarkThemeColors,
        typography = AppTypography,
        content = content
    )
}

@Composable
fun ThemedPreview(
    children: @Composable () -> Unit
) {
    AppTheme {
        Surface {
            children()
        }
    }
}