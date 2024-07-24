package io.github.xlopec.reader.app.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import io.github.xlopec.reader.app.AppState
import io.github.xlopec.reader.app.Message
import io.github.xlopec.reader.app.messageHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

@Composable
fun App(
    component: (Flow<Message>) -> Flow<AppState>,
) {
    val messages = remember { MutableSharedFlow<Message>() }
    val stateFlow = remember { component(messages) }
    val appState = stateFlow.collectAsNullableState(context = Dispatchers.Main).value ?: return
    val scope = rememberCoroutineScope { Dispatchers.Main.immediate }
    val messageHandler = remember { scope.messageHandler(messages) }

    App(
        appState = appState,
        onMessage = messageHandler
    )

    val systemUiController = rememberWindowInsetsController()

    LaunchedEffect(appState.settings.appDarkModeEnabled) {
        systemUiController.isAppearanceLightStatusBars = !appState.settings.appDarkModeEnabled
        systemUiController.isAppearanceLightNavigationBars = !appState.settings.appDarkModeEnabled
    }
}