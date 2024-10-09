package io.github.xlopec.reader.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import io.github.xlopec.reader.app.AppState
import io.github.xlopec.reader.app.Message
import io.github.xlopec.reader.app.command.Command
import io.github.xlopec.reader.app.messageHandler
import io.github.xlopec.reader.app.screen
import io.github.xlopec.reader.app.ui.theme.AppTheme
import io.github.xlopec.tea.core.Regular
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
    val currentState by rememberUpdatedState(snapshot.currentState)

    AppTheme(
        isDarkModeEnabled = currentState.settings.appDarkModeEnabled
    ) {
        val currentScreen by rememberUpdatedState(currentState.screen)
        val previousScreen by rememberUpdatedState((snapshot as? Regular)?.previousState?.screen)
        val previousState by rememberUpdatedState((snapshot as? Regular)?.previousState)
        val transition = updateTransition(targetState = currentScreen, label = "Screen transition")

        transition.AnimatedContent(
            transitionSpec = {
                screenTransition(currentScreen, previousScreen, currentState, previousState)
            },
            contentKey = { it.id.toString() }
        ) { animatedScreen ->
            Screen(
                modifier = Modifier.fillMaxSize(),
                screen = animatedScreen,
                app = currentState,
                handler = handler,
            )
        }
    }

    val systemUiController = rememberWindowInsetsController()

    LaunchedEffect(currentState.settings.appDarkModeEnabled) {
        systemUiController.isAppearanceLightStatusBars = !currentState.settings.appDarkModeEnabled
        systemUiController.isAppearanceLightNavigationBars = !currentState.settings.appDarkModeEnabled
    }
}
