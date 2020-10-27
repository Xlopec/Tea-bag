package com.max.weatherviewer.screens.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.platform.setContent
import com.max.weatherviewer.R
import com.max.weatherviewer.app.*
import com.max.weatherviewer.misc.collect
import com.max.weatherviewer.screens.feed.Feed
import com.max.weatherviewer.screens.home.HomeScreen
import com.max.weatherviewer.ui.theme.lightThemeColors
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect

class MainActivity : AppCompatActivity(), CoroutineScope by MainScope() {

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

private fun ComponentActivity.render(
    screen: Screen,
    onMessage: (Message) -> Unit,
) =
    setContent {

        MaterialTheme(
            colors = lightThemeColors
        ) {
            when (screen) {
                is Feed -> HomeScreen(screen = screen, onMessage = onMessage)
            }
        }
    }