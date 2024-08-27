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

package io.github.xlopec.reader.app.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness5
import androidx.compose.material.icons.outlined.SyncAlt
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import io.github.xlopec.reader.app.MessageHandler
import io.github.xlopec.reader.app.Settings
import io.github.xlopec.reader.app.feature.settings.ToggleDarkMode

@Composable
internal fun Settings(
    innerPadding: PaddingValues,
    settings: Settings,
    onMessage: MessageHandler,
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
            SwitchItem(
                icon = Icons.Default.Brightness5,
                imageDescription = "App dark mode",
                title = "App dark mode",
                description = "Use dark mode in the app",
                checked = settings.userDarkModeEnabled,
                enabled = !settings.syncWithSystemDarkModeEnabled,
            ) {
                onMessage(settings.toToggleDarkModeMessage(userDarkModeEnabled = it))
            }

            SwitchItem(
                icon = Icons.Outlined.SyncAlt,
                imageDescription = "System dark mode",
                title = "System dark mode",
                description = "Use system dark mode",
                checked = settings.syncWithSystemDarkModeEnabled,
            ) {
                onMessage(settings.toToggleDarkModeMessage(syncWithSystemDarkModeEnabled = it))
            }
        }
    }
}

@Composable
private fun SwitchItem(
    icon: ImageVector,
    imageDescription: String?,
    title: String,
    description: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {

        Icon(
            contentDescription = imageDescription,
            tint = MaterialTheme.colors.onSurface,
            imageVector = icon
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.subtitle1
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.body1
            )
        }

        Switch(
            enabled = enabled,
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
    Divider()
}

private fun Settings.toToggleDarkModeMessage(
    userDarkModeEnabled: Boolean = this.userDarkModeEnabled,
    syncWithSystemDarkModeEnabled: Boolean = this.syncWithSystemDarkModeEnabled
) = ToggleDarkMode(
    userDarkModeEnabled = userDarkModeEnabled,
    syncWithSystemDarkModeEnabled = syncWithSystemDarkModeEnabled
)
