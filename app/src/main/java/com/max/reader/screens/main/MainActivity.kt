/*
 * MIT License
 *
 * Copyright (c) 2021. Maksym Oliinyk.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.max.reader.screens.main

import android.os.Bundle
import android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.core.view.WindowCompat
import com.max.reader.R
import com.max.reader.app.*
import com.max.reader.misc.safe
import com.max.reader.screens.article.details.ArticleDetailsState
import com.max.reader.screens.article.details.ui.ArticleDetailsScreen
import com.max.reader.screens.article.list.ArticlesState
import com.max.reader.screens.home.HomeScreen
import com.max.reader.screens.settings.SettingsState
import com.max.reader.ui.theme.AppTheme
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect

class MainActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_NewsReader)
        super.onCreate(savedInstanceState)
        // todo migrate at some point in the future
        @Suppress("DEPRECATION")
        window.setFlags(FLAG_TRANSLUCENT_STATUS, FLAG_TRANSLUCENT_STATUS)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val stateFlow = remember { appComponent(appMessages.asFlow()) }
            val state = stateFlow.collectAsState(context = Dispatchers.Main, initial = null)

            state.value?.render(appMessages::offer)
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

}

@Composable
private fun AppState.render(
    onMessage: (Message) -> Unit,
) {
    AppTheme(
        isDarkModeEnabled = isInDarkMode
    ) {

        BackHandler {
            onMessage(Pop)
        }

        when (val screen = screen) {
            is ArticlesState -> HomeScreen(screen, onMessage)
            is SettingsState -> HomeScreen(this, onMessage)
            is ArticleDetailsState -> ArticleDetailsScreen(screen, onMessage)
            else -> error("unhandled branch $screen")
        }.safe
    }
}
