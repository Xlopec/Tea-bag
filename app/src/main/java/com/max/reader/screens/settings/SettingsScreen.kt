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

package com.max.reader.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness3
import androidx.compose.material.icons.filled.Brightness5
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.max.reader.app.AppState
import com.max.reader.app.Message

@Composable
fun SettingsScreen(
    innerPadding: PaddingValues,
    state: AppState,
    onMessage: (Message) -> Unit,
) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .fillMaxSize()
            .padding(innerPadding)
    ) {

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Icon(
                    contentDescription = "Dark Mode",
                    imageVector = if (state.isDarkModeEnabled) Icons.Default.Brightness3 else Icons.Default.Brightness5
                )

                Column {
                    Text(
                        text = "Dark mode",
                        style = MaterialTheme.typography.subtitle1
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "${if (state.isDarkModeEnabled) "Dis" else "En"}ables dark mode in the app",
                        style = MaterialTheme.typography.body1
                    )
                }

                Spacer(modifier = Modifier.weight(weight = 1f, fill = false))

                Switch(
                    checked = state.isDarkModeEnabled,
                    onCheckedChange = { onMessage(ToggleDarkMode) }
                )
            }
            Divider()
        }
    }
}
