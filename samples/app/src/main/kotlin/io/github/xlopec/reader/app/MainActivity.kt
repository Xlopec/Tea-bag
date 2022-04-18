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
import android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import io.github.xlopec.reader.R
import io.github.xlopec.reader.app.command.CloseApp
import io.github.xlopec.reader.app.command.Command
import io.github.xlopec.reader.app.feature.settings.SystemDarkModeChanged
import io.github.xlopec.reader.app.ui.screens.AppView
import io.github.xlopec.tea.core.ExperimentalTeaApi
import io.github.xlopec.tea.core.observeCommands
import io.github.xlopec.tea.core.states
import io.github.xlopec.tea.core.subscribeIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    private val systemDarkModeChanges = MutableSharedFlow<SystemDarkModeChanged>()

    @OptIn(ExperimentalTeaApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_NewsReader)
        super.onCreate(savedInstanceState)
        // todo migrate at some point in the future
        @Suppress("DEPRECATION")
        window.setFlags(FLAG_TRANSLUCENT_STATUS, FLAG_TRANSLUCENT_STATUS)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            AppView(component.states())
        }

        launch {
            component.observeCommands()
                .filter { it.hasCloseCommand }
                .collect { finishAfterTransition() }
        }

        component.states().subscribeIn(systemDarkModeChanges, this)
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
