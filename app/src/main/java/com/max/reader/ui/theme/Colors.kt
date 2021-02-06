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

package com.max.reader.ui.theme

import androidx.compose.material.lightColors
import androidx.compose.ui.graphics.Color

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
