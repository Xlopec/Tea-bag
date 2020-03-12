@file:Suppress("FunctionName")

package com.oliynick.max.tea.core.debug.component

import com.oliynick.max.tea.core.Env
import com.oliynick.max.tea.core.EnvBuilder
import com.oliynick.max.tea.core.debug.session.SessionBuilder
import com.oliynick.max.tea.core.debug.session.WebSocketSession
import com.oliynick.max.tea.core.debug.session.localhost
import com.oliynick.max.tea.core.toEnv
import protocol.ComponentId
import protocol.JsonConverter
import java.net.URL

@DslMarker
private annotation class DslBuilder

data class DebugEnv<M, S, C, J>(
    inline val componentEnv: Env<M, S, C>,
    inline val serverSettings: ServerSettings<M, S, J>
)

data class ServerSettings<M, S, J>(
    inline val id: ComponentId,
    inline val serializer: JsonConverter<J>,
    inline val url: URL,
    inline val sessionBuilder: SessionBuilder<M, S, J>
)

@DslBuilder
class ServerSettingsBuilder<M, S, J> @PublishedApi internal constructor(
    val id: ComponentId,
    var jsonSerializer: JsonConverter<J>,
    var sessionBuilder: SessionBuilder<M, S, J>,
    var url: URL = localhost
)

@DslBuilder
class DebugEnvBuilder<M, S, C, J> @PublishedApi internal constructor(
    var envBuilder: EnvBuilder<M, S, C>,
    var serverSettingsBuilder: ServerSettingsBuilder<M, S, J>
) {

    fun environment(
        config: EnvBuilder<M, S, C>.() -> Unit
    ) {
        envBuilder.apply(config)
    }

    fun serverSettings(
        config: ServerSettingsBuilder<M, S, J>.() -> Unit
    ) {
        serverSettingsBuilder.apply(config)
    }

}

inline fun <reified M, reified C, reified S, J> Dependencies(
    id: ComponentId,
    env: EnvBuilder<M, S, C>,
    jsonConverter: JsonConverter<J>,
    config: DebugEnvBuilder<M, S, C, J>.() -> Unit = {}
) = DebugEnvBuilder(
    env,
    ServerSettingsBuilder(id, jsonConverter, ::WebSocketSession)
).apply(config).toDebugDependencies()

@PublishedApi
internal inline fun <reified M, reified C, reified S, J> DebugEnvBuilder<M, S, C, J>.toDebugDependencies() =
    DebugEnv(
        envBuilder.toEnv(),
        serverSettingsBuilder.toServerSettings()
    )

@PublishedApi
internal inline fun <reified M, reified S, J> ServerSettingsBuilder<M, S, J>.toServerSettings() =
    ServerSettings(
        id,
        jsonSerializer,
        url,
        sessionBuilder
    )
