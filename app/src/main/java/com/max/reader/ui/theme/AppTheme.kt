package com.max.reader.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import dev.chrisbanes.accompanist.insets.ProvideWindowInsets

@Composable
fun AppTheme(
    isDarkModeEnabled: Boolean,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colors = if (isDarkModeEnabled) AppDarkThemeColors else AppLightThemeColors,
        typography = AppTypography,
    ) {
        ProvideWindowInsets {
            content()
        }
    }
}

@Composable
fun ThemedPreview(
    content: @Composable () -> Unit,
) {
    AppTheme(isDarkModeEnabled = true) {
        Surface {
            content()
        }
    }
}
