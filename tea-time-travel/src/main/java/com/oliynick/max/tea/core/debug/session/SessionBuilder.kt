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

package com.oliynick.max.tea.core.debug.session

import com.oliynick.max.tea.core.debug.component.ServerSettings
import com.oliynick.max.tea.core.debug.component.URL
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.websocket.*
import io.ktor.http.*
import java.net.URL

/**
 * Function that for a given server settings creates a new connection
 * to a debug server
 *
 * @param M message type
 * @param S state type
 * @param J json type
 */
public typealias SessionBuilder<M, S, J> = suspend (
    settings: ServerSettings<M, S, J>,
    session: suspend DebugSession<M, S, J>.() -> Unit,
) -> Unit

/**
 * Creates a new web socket session using supplied settings
 *
 * @param settings server settings
 * @param block lambda to interact with [session][DebugSession]
 * @param M message type
 * @param S state type
 * @param J json type
 */
public suspend inline fun <reified M, reified S, J> WebSocketSession(
    settings: ServerSettings<M, S, J>,
    crossinline block: suspend DebugSession<M, S, J>.() -> Unit,
) {
    HttpClient.ws(// todo add timeout
        method = HttpMethod.Get,
        host = settings.url.host,
        port = settings.url.port,
        block = {
            DebugWebSocketSession(
                M::class.java,
                S::class.java,
                settings,
                this
            ).apply { block() }
        }
    )
}

@PublishedApi
internal val HttpClient: HttpClient by lazy { HttpClient(CIO) { install(WebSockets) } }

@PublishedApi
internal val Localhost: URL by lazy(::URL)
