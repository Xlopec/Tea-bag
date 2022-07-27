/*
 * Copyright (C) 2021. Maksym Oliinyk.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.xlopec.tea.time.travel.plugin.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import com.intellij.ide.ui.UISettings
import io.kanro.compose.jetbrains.JBTheme
import io.kanro.compose.jetbrains.JBTypography
import io.kanro.compose.jetbrains.color.PanelColors
import io.kanro.compose.jetbrains.color.TextColors
import io.kanro.compose.jetbrains.control.JPanel

private val PreviewPanelColors = PanelColors(Color.Black, Color.Gray, Color(45, 48, 50))

@Suppress("unused")
val JBTheme.contrastBorderColor: Color
    @ReadOnlyComposable
    @Composable
    get() = LocalPluginColors.current.contrastBorderColor

@Composable
fun PluginTheme(
    content: @Composable () -> Unit,
) {
    val themeColors = PluginThemeColors()
    val jbTypography = JBTypography(themeColors.textColors, UISettings.getInstance().fontSize)

    JBTheme(
        textColors = themeColors.textColors,
        panelColors = themeColors.panelColors,
        typography = jbTypography,
        fieldColors = themeColors.fieldColors,
        tabColors = themeColors.tabColors,
        checkBoxColors = themeColors.checkBoxColors,
        selectionColors = themeColors.selectionColors,
        buttonColors = themeColors.buttonColors,
        toolBarColors = themeColors.toolbarColors,
        scrollColors = themeColors.scrollColors
    ) {
        CompositionLocalProvider(LocalPluginColors provides themeColors) {
            content()
        }
    }
}

private fun JBTypography(
    textColors: TextColors,
    defaultFontSize: Int
): JBTypography {
    val h2 = TextStyle(
        color = textColors.default,
        fontWeight = FontWeight.Normal,
        fontSize = (defaultFontSize + 5).sp,
    )

    val h3 = TextStyle(
        color = textColors.default,
        fontWeight = FontWeight.Normal,
        fontSize = (defaultFontSize + 3).sp,
    )

    val defaultFontFamily = FontFamily.Default

    val default = TextStyle(
        color = textColors.default,
        fontWeight = FontWeight.Normal,
        fontSize = defaultFontSize.sp,
    )

    val medium = TextStyle(
        color = textColors.default,
        fontWeight = FontWeight.Normal,
        fontSize = (defaultFontSize - 1).sp,
    )
    val small = TextStyle(
        color = textColors.default,
        fontWeight = FontWeight.Normal,
        fontSize = (defaultFontSize - 2).sp,
    )
    return JBTypography(
        defaultFontFamily = defaultFontFamily,
        h0 = TextStyle(
            color = textColors.default,
            fontWeight = FontWeight.Medium,
            fontSize = (defaultFontSize + 12).sp
        ),
        h1 = TextStyle(
            color = textColors.default,
            fontWeight = FontWeight.Medium,
            fontSize = (defaultFontSize + 9).sp
        ),
        h2 = h2,
        h2Bold = h2.copy(fontWeight = FontWeight.Medium),
        h3 = h3,
        h3Bold = h3.copy(fontWeight = FontWeight.Medium),
        default = default,
        defaultBold = default.copy(fontWeight = FontWeight.Medium),
        defaultUnderlined = default.copy(textDecoration = TextDecoration.Underline),
        paragraph = TextStyle(
            color = textColors.default,
            fontWeight = FontWeight.Normal,
            fontSize = defaultFontSize.sp,
        ),
        medium = medium,
        mediumBold = medium.copy(fontWeight = FontWeight.Medium),
        small = small,
        smallUnderlined = small.copy(textDecoration = TextDecoration.Underline)
    )
}

@Composable
fun PluginPreviewTheme(
    content: @Composable () -> Unit
) {
    JBTheme(panelColors = PreviewPanelColors) {
        JPanel {
            content()
        }
    }
}
