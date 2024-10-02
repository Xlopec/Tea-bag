package io.github.xlopec.reader.app.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import io.github.xlopec.reader.app.AppState
import io.github.xlopec.reader.app.Message
import io.github.xlopec.reader.app.messageHandler
import io.github.xlopec.reader.app.screen
import io.github.xlopec.reader.app.ui.theme.AppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

@Composable
public fun App(
    component: (Flow<Message>) -> Flow<AppState>,
) {
    val messages = remember { MutableSharedFlow<Message>() }
    val stateFlow = remember { component(messages) }
    val app = stateFlow.collectAsNullableState(context = Dispatchers.Main).value ?: return
    val scope = rememberCoroutineScope { Dispatchers.Main.immediate }
    val handler = remember { scope.messageHandler(messages) }

    AppTheme(
        isDarkModeEnabled = app.settings.appDarkModeEnabled
    ) {
        Screen(
            modifier = Modifier.fillMaxSize(),
            screen = app.screen,
            app = app,
            handler = handler,
        )
    }

    val systemUiController = rememberWindowInsetsController()

    LaunchedEffect(app.settings.appDarkModeEnabled) {
        systemUiController.isAppearanceLightStatusBars = !app.settings.appDarkModeEnabled
        systemUiController.isAppearanceLightNavigationBars = !app.settings.appDarkModeEnabled
    }
}
