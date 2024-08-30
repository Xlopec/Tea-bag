package io.github.xlopec.reader.app.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.window.ComposeUIViewController
import io.github.xlopec.reader.app.IosComponent
import io.github.xlopec.reader.app.Message
import io.github.xlopec.reader.app.messageHandler
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
    val appState = stateFlow.collectAsNullableState(context = Dispatchers.Main).value ?: return
    val scope = rememberCoroutineScope { Dispatchers.Main.immediate }
    val messageHandler = remember { scope.messageHandler(messages) }

    App(
        app = appState,
        handler = messageHandler
    )
}
