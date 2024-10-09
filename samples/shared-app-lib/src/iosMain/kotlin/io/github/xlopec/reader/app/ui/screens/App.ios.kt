package io.github.xlopec.reader.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController
import com.arkivanov.essenty.backhandler.BackDispatcher
import io.github.xlopec.reader.app.FullScreen
import io.github.xlopec.reader.app.IosComponent
import io.github.xlopec.reader.app.Message
import io.github.xlopec.reader.app.Screen
import io.github.xlopec.reader.app.feature.navigation.Pop
import io.github.xlopec.reader.app.messageHandler
import io.github.xlopec.reader.app.screen
import io.github.xlopec.reader.app.ui.theme.AppTheme
import io.github.xlopec.tea.core.Regular
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
    val snapshots = remember { component.component(messages) }
    val snapshot = snapshots.collectAsNullableState(context = Dispatchers.Main).value ?: return
    val scope = rememberCoroutineScope { Dispatchers.Main.immediate }
    val handler = remember { scope.messageHandler(messages) }
    val currentState by rememberUpdatedState(snapshot.currentState)

    AppTheme(
        isDarkModeEnabled = currentState.settings.appDarkModeEnabled
    ) {
        val backDispatcher = remember { BackDispatcher() }

        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
        ) {
            val animation = rememberDefaultPredictiveBackAnimation(screenWidth = maxWidth)
            val coordinator = rememberPredictiveBackCoordinator(
                dispatcher = backDispatcher,
                stack = currentState.screens,
                animation = animation,
                previousScreenFor = { stack, current ->
                    stack.getOrNull<Screen>(stack.lastIndex - 1)?.takeIf<Screen> { current is FullScreen }
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
                val currentScreen by rememberUpdatedState(screen)
                val previousScreen by rememberUpdatedState((snapshot as? Regular)?.previousState?.screen)
                val previousState by rememberUpdatedState((snapshot as? Regular)?.previousState)
                val transition = updateTransition(targetState = screen, label = "Screen transition")

                transition.AnimatedContent(
                    transitionSpec = {
                        screenTransition(currentScreen, previousScreen, currentState, previousState)
                    },
                    contentKey = { it.id.toString() }
                ) { animatedScreen ->
                    Screen(
                        modifier = modifier,
                        screen = animatedScreen,
                        app = currentState,
                        handler = handler,
                    )
                }
            }
        }
    }
}
