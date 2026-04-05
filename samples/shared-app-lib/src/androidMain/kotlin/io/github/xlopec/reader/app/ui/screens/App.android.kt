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

package io.github.xlopec.reader.app.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import io.github.xlopec.reader.app.AppState
import io.github.xlopec.reader.app.Message
import io.github.xlopec.reader.app.command.Command
import io.github.xlopec.reader.app.messageHandler
import io.github.xlopec.reader.app.screen
import io.github.xlopec.reader.app.ui.theme.AppTheme
import io.github.xlopec.tea.core.Snapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

@Composable
public fun App(
    component: (Flow<Message>) -> Flow<Snapshot<Message, AppState, Command>>,
) {
    val messages = remember { MutableSharedFlow<Message>() }
    val snapshots = remember { component(messages) }
    val snapshot = snapshots.collectAsNullableState(context = Dispatchers.Main).value ?: return
    val scope = rememberCoroutineScope { Dispatchers.Main.immediate }
    val handler = remember { scope.messageHandler(messages) }
    val currentState = snapshot.currentState

    AppTheme(
        isDarkModeEnabled = currentState.settings.appDarkModeEnabled
    ) {
        ScreenTransition(
            modifier = Modifier.fillMaxSize(),
            screen = currentState.screen,
            snapshot = snapshot,
            handler = handler,
        )
    }

    val systemUiController = rememberWindowInsetsController()

    LaunchedEffect(currentState.settings.appDarkModeEnabled) {
        systemUiController.isAppearanceLightStatusBars = !currentState.settings.appDarkModeEnabled
        systemUiController.isAppearanceLightNavigationBars = !currentState.settings.appDarkModeEnabled
    }
}
