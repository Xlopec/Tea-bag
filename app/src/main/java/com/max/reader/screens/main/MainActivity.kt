/*
 * Copyright (C) 2021. Maksym Oliinyk.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
        isDarkModeEnabled = isDarkModeEnabled
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