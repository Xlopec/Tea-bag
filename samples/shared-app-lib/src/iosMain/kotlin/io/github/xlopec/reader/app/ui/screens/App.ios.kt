package io.github.xlopec.reader.app.ui.screens

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController
import com.arkivanov.essenty.backhandler.BackDispatcher
import io.github.xlopec.reader.app.FullScreen
import io.github.xlopec.reader.app.IosComponent
import io.github.xlopec.reader.app.Message
import io.github.xlopec.reader.app.feature.navigation.Pop
import io.github.xlopec.reader.app.messageHandler
import io.github.xlopec.reader.app.ui.theme.AppTheme
import io.github.xlopec.tea.navigation.PredictiveBackContainer
import io.github.xlopec.tea.navigation.rememberDefaultPredictiveBackAnimation
import io.github.xlopec.tea.navigation.rememberPredictiveBackCoordinator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import platform.UIKit.UIViewController
import kotlin.experimental.ExperimentalObjCName

@OptIn(ExperimentalObjCName::class)
@ObjCName("appController")
public fun AppController(
    component: IosComponent
): UIViewController = ComposeUIViewController {
    App(component)
}

@Composable
internal fun App(
    component: IosComponent,
) {
    val messages = remember { MutableSharedFlow<Message>() }
    val stateFlow = remember { component.component(messages) }
    val app = stateFlow.collectAsNullableState(context = Dispatchers.Main).value ?: return
    val scope = rememberCoroutineScope { Dispatchers.Main.immediate }
    val handler = remember { scope.messageHandler(messages) }

    AppTheme(
        isDarkModeEnabled = app.settings.appDarkModeEnabled
    ) {
        val backDispatcher = remember { BackDispatcher() }

        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
        ) {
            val animation = rememberDefaultPredictiveBackAnimation(screenWidth = maxWidth)
            val coordinator = rememberPredictiveBackCoordinator(
                dispatcher = backDispatcher,
                stack = app.screens,
                animation = animation,
                previousScreenFor = { stack, current ->
                    stack.getOrNull(stack.lastIndex - 1)?.takeIf { current is FullScreen }
                },
                onBackComplete = {
                    handler(Pop)
                },
            )

            PredictiveBackContainer(
                modifier = Modifier.fillMaxSize(),
                backDispatcher = backDispatcher,
                endEdgeEnabled = false,
                coordinator = coordinator,
            ) { modifier, screen ->
                Screen(
                    modifier = modifier,
                    screen = screen,
                    app = app,
                    handler = handler,
                )
            }
        }
    }
}
