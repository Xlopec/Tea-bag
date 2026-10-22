/*
 * MIT License
 *
 * Copyright (c) 2026. Maksym Oliinyk.
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

@file:Suppress("FunctionName")

package io.github.xlopec.reader.app

import android.Manifest.permission.ACCESS_LOCAL_NETWORK
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.CINNAMON_BUN
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import io.github.xlopec.reader.app.ui.screens.App
import io.github.xlopec.reader.app.ui.theme.AppTheme

@Composable
public fun RemoteApp(
    entryPoint: () -> AppEntryPoint,
) {
    if (SDK_INT < CINNAMON_BUN) {
        App(entryPoint = remember { entryPoint() })
    } else {
        val context = LocalContext.current
        var granted by rememberSaveable { mutableStateOf(if (context.hasLocalNetworkAccess) true else null) }
        var requested by rememberSaveable { mutableStateOf(false) }
        val request = rememberLauncherForActivityResult(RequestPermission()) { granted = it }

        // A grant restored from saved state can be stale: revoking access in Settings kills
        // the process, and the restored `true` would start the component without it. A
        // pending request (`null`) is left alone so an open dialog isn't disturbed.
        LifecycleResumeEffect(Unit) {
            if (context.hasLocalNetworkAccess) {
                granted = true
            } else if (granted == true) {
                granted = false
            }
            onPauseOrDispose {}
        }

        when {
            granted == true && context.hasLocalNetworkAccess -> App(entryPoint = remember { entryPoint() })
            granted == null -> LaunchedEffect(Unit) {
                if (!requested) {
                    requested = true
                    request.launch(ACCESS_LOCAL_NETWORK)
                }
            }
            else -> LocalNetworkAccessError(onOpenSettings = { context.openAppSettings() })
        }
    }
}

private val Context.hasLocalNetworkAccess: Boolean
    @RequiresApi(CINNAMON_BUN)
    get() = checkSelfPermission(ACCESS_LOCAL_NETWORK) == PERMISSION_GRANTED

private fun Context.openAppSettings() =
    startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null)))

@Composable
private fun LocalNetworkAccessError(
    onOpenSettings: () -> Unit,
) {
    AppTheme(isDarkModeEnabled = isSystemInDarkTheme()) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Local network access is required",
                    style = MaterialTheme.typography.h6,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "The time-travel debugger runs on your computer, which the app reaches over the local network. " +
                        "Allow local network access in the app settings",
                    textAlign = TextAlign.Center,
                )
                Button(onClick = onOpenSettings) {
                    Text(text = "Open settings")
                }
            }
        }
    }
}
