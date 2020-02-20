@file:Suppress("FunctionName")

package com.oliynick.max.tea.core.debug.session

import com.oliynick.max.tea.core.debug.component.ServerSettings
import com.oliynick.max.tea.core.debug.component.URL
import io.ktor.client.HttpClient
import io.ktor.client.features.websocket.WebSockets
import io.ktor.client.features.websocket.ws
import io.ktor.http.HttpMethod

typealias SessionBuilder<M, S> = suspend (ServerSettings<M, S>, suspend DebugSession<M, S>.() -> Unit) -> Unit

suspend inline fun <reified M, reified S> WebSocketSession(
    settings: ServerSettings<M, S>,
    crossinline block: suspend DebugSession<M, S>.() -> Unit
) = httpClient.ws(
    method = HttpMethod.Get,
    host = settings.url.host,
    port = settings.url.port,
    block = { DebugWebSocketSession(
        M::class.java,
        S::class.java,
        settings,
        this
    ).apply { block() } }
)

@PublishedApi
internal val httpClient by lazy { HttpClient { install(WebSockets) } }

@PublishedApi
internal val localhost by lazy(::URL)