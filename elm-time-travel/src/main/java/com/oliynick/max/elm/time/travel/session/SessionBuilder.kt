package com.oliynick.max.elm.time.travel.component

import com.oliynick.max.elm.time.travel.session.DebugSession
import com.oliynick.max.elm.time.travel.session.DebugWebSocketSession
import io.ktor.client.HttpClient
import io.ktor.client.features.websocket.WebSockets
import io.ktor.client.features.websocket.ws
import io.ktor.http.HttpMethod

typealias SessionBuilder<M, S> = suspend (ServerSettings, suspend DebugSession<M, S>.() -> Unit) -> Unit

suspend inline fun <reified M, reified S> WebSocketSession(
    settings: ServerSettings,
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