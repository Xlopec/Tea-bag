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

@file:Suppress("FunctionName")

package io.github.xlopec.reader.app

import io.github.xlopec.tea.core.toStatesComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

public fun interface Cancellation {
    public fun cancel()
}

public class IosComponent(
    systemDarkModeEnabled: Boolean
) {
    private val componentJob = Job()
    private val componentScope = CoroutineScope(Main + componentJob)
    private val component = AppComponent(systemDarkModeEnabled, componentScope).toStatesComponent()
    private val messages = MutableSharedFlow<Message>()

    public fun dispatch(
        message: Message
    ) {
        componentScope.launch {
            messages.emit(message)
        }
    }

    public fun destroy() {
        componentScope.cancel()
    }

    public fun render(
        renderCallback: (AppState) -> Unit
    ): Cancellation {

        val renderScope = CoroutineScope(Main + Job(parent = componentJob))

        component(messages)
            .onEach { renderCallback(it) }
            .launchIn(renderScope)

        return Cancellation { renderScope.cancel() }
    }
}

private fun AppComponent(
    systemDarkModeEnabled: Boolean,
    coroutineScope: CoroutineScope
) = Environment(coroutineScope)
    .let { AppComponent(it, AppInitializer(systemDarkModeEnabled, it)) }
