/*
 * MIT License
 *
 * Copyright (c) 2022. Maksym Oliinyk.
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

package io.github.xlopec.reader.app

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import io.github.xlopec.reader.R
import io.github.xlopec.reader.app.command.CloseApp
import io.github.xlopec.reader.app.command.Command
import io.github.xlopec.reader.app.feature.settings.SystemDarkModeChanged
import io.github.xlopec.reader.app.ui.screens.App
import io.github.xlopec.tea.core.ExperimentalTeaApi
import io.github.xlopec.tea.core.subscribeIn
import io.github.xlopec.tea.core.toCommandsFlow
import io.github.xlopec.tea.core.toStatesComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity(), CoroutineScope by MainScope() {

    private val systemDarkModeChanges = MutableSharedFlow<SystemDarkModeChanged>()

    @OptIn(ExperimentalTeaApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.NewsReader)
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val statesComponent = component.toStatesComponent()

        setContent {
            App(component = statesComponent)
        }

        launch {
            component.toCommandsFlow()
                .filter { it.hasCloseCommand }
                .collect { finishAfterTransition() }
        }

        statesComponent.subscribeIn(systemDarkModeChanges, this)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        launch {
            systemDarkModeChanges.emit(SystemDarkModeChanged(newConfig.systemDarkModeEnabled))
        }
    }

    override fun onDestroy() {
        cancel()
        super.onDestroy()
    }
}

private val Collection<Command>.hasCloseCommand: Boolean
    get() = CloseApp in this
