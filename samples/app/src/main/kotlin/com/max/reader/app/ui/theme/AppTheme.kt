/*
 * MIT License
 *
 * Copyright (c) 2022. Maksym Oliinyk.
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

package com.max.reader.app.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.google.accompanist.insets.ProvideWindowInsets

@Composable
fun AppTheme(
    isDarkModeEnabled: Boolean,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colors = appColors(isDarkModeEnabled),
        typography = Typography,
    ) {
        ProvideWindowInsets {
            content()
        }
    }
}

@Composable
private fun appColors(
    isDarkModeEnabled: Boolean
): Colors {
    //todo refactor
    val primary by animateColorAsState(if (!isDarkModeEnabled) LightThemeColors.primary else DarkThemeColors.primary)
    val primaryVariant by animateColorAsState(if (!isDarkModeEnabled) LightThemeColors.primaryVariant else DarkThemeColors.primaryVariant)
    val secondary by animateColorAsState(if (!isDarkModeEnabled) LightThemeColors.secondary else DarkThemeColors.secondary)
    val secondaryVariant by animateColorAsState(if (!isDarkModeEnabled) LightThemeColors.secondaryVariant else DarkThemeColors.secondaryVariant)
    val background by animateColorAsState(if (!isDarkModeEnabled) LightThemeColors.background else DarkThemeColors.background)
    val surface by animateColorAsState(if (!isDarkModeEnabled) LightThemeColors.surface else DarkThemeColors.surface)
    val error by animateColorAsState(if (!isDarkModeEnabled) LightThemeColors.error else DarkThemeColors.error)
    val onPrimary by animateColorAsState(if (!isDarkModeEnabled) LightThemeColors.onPrimary else DarkThemeColors.onPrimary)
    val onSecondary by animateColorAsState(if (!isDarkModeEnabled) LightThemeColors.onSecondary else DarkThemeColors.onSecondary)
    val onBackground by animateColorAsState(if (!isDarkModeEnabled) LightThemeColors.onBackground else DarkThemeColors.onBackground)
    val onSurface by animateColorAsState(if (!isDarkModeEnabled) LightThemeColors.onSurface else DarkThemeColors.onSurface)
    val onError by animateColorAsState(if (!isDarkModeEnabled) LightThemeColors.onError else DarkThemeColors.onError)

    return Colors(
        primary = primary,
        primaryVariant = primaryVariant,
        secondary = secondary,
        secondaryVariant = secondaryVariant,
        background = background,
        surface = surface,
        error = error,
        onPrimary = onPrimary,
        onSecondary = onSecondary,
        onBackground = onBackground,
        onSurface = onSurface,
        onError = onError,
        isLight = !isDarkModeEnabled
    )
}

@Composable
fun ThemedPreview(
    isDarkModeEnabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    AppTheme(isDarkModeEnabled = isDarkModeEnabled) {
        Surface {
            content()
        }
    }
}
