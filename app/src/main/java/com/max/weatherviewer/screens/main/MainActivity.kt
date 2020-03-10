package com.max.weatherviewer.screens.main

import android.app.Activity
import android.os.Bundle
import androidx.ui.core.setContent
import com.max.weatherviewer.R
import com.max.weatherviewer.app.Message
import com.max.weatherviewer.app.Pop
import com.max.weatherviewer.app.Screen
import com.max.weatherviewer.app.appComponent
import com.max.weatherviewer.app.appMessages
import com.max.weatherviewer.app.closeAppCommands
import com.max.weatherviewer.app.screen
import com.max.weatherviewer.misc.collect
import com.max.weatherviewer.screens.feed.Feed
import com.max.weatherviewer.screens.home.HomeScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : Activity(), CoroutineScope by MainScope() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_NewsReader)
        super.onCreate(savedInstanceState)

        launch {
            appComponent(appMessages.asFlow()).collect(Dispatchers.Main) { state ->
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

private fun Activity.render(
    screen: Screen,
    onMessage: (Message) -> Unit
) =
    setContent {

        when (screen) {
            is Feed -> HomeScreen(screen, onMessage)
        }
    }