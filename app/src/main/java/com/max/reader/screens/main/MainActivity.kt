package com.max.reader.screens.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.setContent
import com.max.reader.R
import com.max.reader.app.*
import com.max.reader.misc.collect
import com.max.reader.misc.safe
import com.max.reader.screens.article.details.ArticleDetailsState
import com.max.reader.screens.article.details.ui.ArticleDetailsScreen
import com.max.reader.screens.article.list.ArticlesState
import com.max.reader.screens.home.HomeScreen
import com.max.reader.ui.theme.AppTheme
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
    screen: ScreenState,
    onMessage: (Message) -> Unit,
) =
    setContent {
        AppTheme {
            when (screen) {
                is ArticlesState -> HomeScreen(screen, onMessage)
                is ArticleDetailsState -> ArticleDetailsScreen(screen, onMessage)
                else -> error("unhandled branch $screen")
            }.safe
        }
    }