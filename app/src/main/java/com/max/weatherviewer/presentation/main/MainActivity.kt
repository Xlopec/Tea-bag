package com.max.weatherviewer.presentation.main

import android.app.Activity
import android.os.Bundle
import androidx.ui.core.setContent
import com.max.weatherviewer.*
import com.max.weatherviewer.app.appComponent
import com.max.weatherviewer.app.appMessages
import com.max.weatherviewer.app.closeAppCommands
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow

class MainActivity : Activity(), CoroutineScope by MainScope() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        launch {
            appComponent(appMessages.consumeAsFlow()).collect(Dispatchers.Main) { state ->
                render(state.screen, appMessages::offer)
            }
        }

        launch {
            closeAppCommands.collect {
                finishAfterTransition()
            }
        }
    }

    override fun onDestroy() {
        cancel()
        super.onDestroy()
    }

    override fun onBackPressed() {
        appMessages.offer(Pop)
    }

}

private fun Activity.render(screen: Screen, onMessage: (Message) -> Unit) =
    setContent {
        App(screen, onMessage) {
            Screen(screen, onMessage)
        }
    }