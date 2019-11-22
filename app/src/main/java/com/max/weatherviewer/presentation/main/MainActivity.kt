package com.max.weatherviewer.presentation.main

import android.app.Activity
import android.os.Bundle
import androidx.ui.core.setContent
import com.max.weatherviewer.*
import com.max.weatherviewer.app.ComponentScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch

class MainActivity : Activity(), CoroutineScope by MainScope() {

    private val component = ComponentScope.appComponent(Dependencies(this))
    private val messages = Channel<Message>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        launch {
            component(messages.consumeAsFlow()).collect { state ->
                render(state.screen) { messages.offer(it) }
            }
        }
    }

    override fun onDestroy() {
        cancel()
        super.onDestroy()
    }

    override fun onBackPressed() {
        messages.offer(Pop)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        //direct.instance<PermissionPublisher>().offer(PermissionResult(requestCode, permissions, grantResults))
    }

}

private fun Activity.render(screen: Screen, onMessage: (Message) -> Unit) {
    setContent {
        App {
            Screen(screen, onMessage)
        }
    }
}

private val State.screen: Screen
    get() = screens.last()