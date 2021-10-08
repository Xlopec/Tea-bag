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

@file:Suppress("FunctionName")

package com.oliynick.max.reader.app

import com.oliynick.max.tea.core.Initializer
import com.oliynick.max.tea.core.component.Component
import com.oliynick.max.tea.core.component.states
import com.oliynick.max.tea.core.component.with
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlin.coroutines.CoroutineContext

@OptIn(ExperimentalStdlibApi::class)
fun AppComponent(
    environment: Environment,
    initializer: Initializer<AppState, Command>,
): (Flow<Message>) -> Flow<AppState> =
    Component(
        initializer = initializer,
        resolver = { c -> with(environment) { resolve(c) } },
        updater = { m, s -> with(environment) { update(m, s) } },
        scope = environment,
        io = Dispatchers.Default,//fixme make IO
        computation = environment.coroutineContext[CoroutineDispatcher.Key] ?: Dispatchers.Default,
    ).with { println("New snapshot: $it") }.states()

object IosAppComponentScope : CoroutineScope {
    override val coroutineContext: CoroutineContext =
        Job() + Dispatchers.Default
}

class IosComponentWrapper(
    env: PlatformEnv
) {

    private val component = Environment(env)
        .let { env -> AppComponent(env, AppInitializer(env)) }

    private val messages = MutableSharedFlow<Message>()

    fun send(
        message: Message
    ) {
        IosAppComponentScope.launch {
            println("Emit $message")
            messages.emit(message)
        }
    }

    fun render(
        renderCallback: (AppState) -> Unit
    ) {
        IosAppComponentScope.launch {
            component(messages).collect { state ->
                renderCallback(state)
            }
        }
    }

}
