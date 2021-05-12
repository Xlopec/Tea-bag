/*
 * MIT License
 *
 * Copyright (c) 2021. Maksym Oliinyk.
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

package com.max.reader.ui.theme

import androidx.compose.material.lightColors
import androidx.compose.ui.graphics.Color
// todo fix colors, should be [darkColors]
val AppDarkThemeColors = lightColors(
    primary = Color(0xFF263238),
    primaryVariant = Color(0xFF1B272C),
    onPrimary = Color.White,
    secondary = Color(0xFFFFFFFF),
    secondaryVariant = Color(0xFFFFFFFF),
    onSecondary = Color.White,
    background = Color(0xFF151B1F),
    onBackground = Color.White,
    surface = Color(0xFF1F282E),
    onSurface = Color.White,
    error = Color(0xFFF70040),
    onError = Color.White
)

val AppLightThemeColors = lightColors(
    primary = Color(0xFF263238),
    primaryVariant = Color(0xFF000a12),
    secondaryVariant = Color(0xFF1c313a),
    onPrimary = Color.White,
    secondary = Color(0xFF455a64),
    onSecondary = Color.Black,
    background = Color.White,
    onBackground = Color.Black,
    surface = Color(0xFFffffff),
    onSurface = Color.Black,
    error = Color(0xFFD00036),
    onError = Color.White
)
