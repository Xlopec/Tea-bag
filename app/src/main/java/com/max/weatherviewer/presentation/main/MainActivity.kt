package com.max.weatherviewer.presentation.main

import android.app.Activity
import android.os.Bundle
import androidx.ui.core.setContent
import com.max.weatherviewer.*
import com.max.weatherviewer.app.ComponentScope
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow

class MainActivity : Activity(), CoroutineScope by MainScope() {

    private val dependencies = Dependencies(
        Channel(),
        newsApi(retrofit { adapters.forEach { (cl, adapter) -> registerTypeAdapter(cl.java, adapter) } })
    )
    private val component = ComponentScope.appComponent(dependencies)
    private val messages = Channel<Message>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        launch {
            component(messages.consumeAsFlow()).collect { state ->
                withContext(Dispatchers.Main) {
                    render(state.screen, messages::offer)
                }
            }
        }

        launch {
            dependencies.closeAppCommands.consumeAsFlow().collect {
                finishAfterTransition()
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
    println("Thread ${Thread.currentThread()}")
    setContent {
        App {
            Screen(screen, onMessage)
        }
    }
}