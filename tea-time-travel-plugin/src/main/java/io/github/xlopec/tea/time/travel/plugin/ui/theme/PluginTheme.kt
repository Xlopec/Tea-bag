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
import org.jetbrains.jewel.bridge.theme.SwingBridgeTheme
import org.jetbrains.jewel.foundation.ExperimentalJewelApi

@OptIn(ExperimentalJewelApi::class)
@Composable
fun PluginTheme(
    content: @Composable () -> Unit,
) {
    SwingBridgeTheme(content)
}

@Composable
fun PluginPreviewTheme(
    content: @Composable () -> Unit
) {
    PluginTheme(content)
}
