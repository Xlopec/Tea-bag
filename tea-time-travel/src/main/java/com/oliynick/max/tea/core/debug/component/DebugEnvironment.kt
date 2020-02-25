@file:Suppress("FunctionName")

package com.oliynick.max.tea.core.debug.component

import com.oliynick.max.tea.core.Env
import com.oliynick.max.tea.core.EnvBuilder
import protocol.JsonConverter
import com.oliynick.max.tea.core.debug.session.SessionBuilder
import com.oliynick.max.tea.core.debug.session.WebSocketSession
import com.oliynick.max.tea.core.debug.session.localhost
import com.oliynick.max.tea.core.toEnv
import protocol.ComponentId
import java.net.URL

@DslMarker
private annotation class DslBuilder

data class DebugEnv<M, C, S, J>(
    inline val componentEnv: Env<M, C, S>,
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
    @PublishedApi
    internal var jsonSerializer: JsonConverter<J>
) {

    @PublishedApi
    internal var url: URL? = null
    @PublishedApi
    internal var sessionBuilder: SessionBuilder<M, S, J>? = null

    fun url(
        u: URL
    ) {
        url = u
    }

    fun installSessionBuilder(
        builder: SessionBuilder<M, S, J>
    ) {
        sessionBuilder = builder
    }

    fun installSerializer(
        serializer: JsonConverter<J>
    ) {
        jsonSerializer = serializer
    }

}

@DslBuilder
class DebugEnvBuilder<M, C, S, J> @PublishedApi internal constructor(
    var dependenciesBuilder: EnvBuilder<M, C, S>,
    var serverSettingsBuilder: ServerSettingsBuilder<M, S, J>
) {

    fun dependencies(config: EnvBuilder<M, C, S>.() -> Unit) {
        dependenciesBuilder.apply(config)
    }

    fun serverSettings(config: ServerSettingsBuilder<M, S, J>.() -> Unit) {
        serverSettingsBuilder.apply(config)
    }

}

inline fun <reified M, reified C, reified S, J> Dependencies(
    id: ComponentId,
    env: Env<M, C, S>,
    jsonConverter: JsonConverter<J>,
    config: DebugEnvBuilder<M, C, S, J>.() -> Unit = {}
) = DebugEnvBuilder(
    EnvBuilder(env),
    ServerSettingsBuilder(id, jsonConverter)
).apply(config).toDebugDependencies()

@PublishedApi
internal inline fun <reified M, reified C, reified S, J> DebugEnvBuilder<M, C, S, J>.toDebugDependencies() =
    DebugEnv(
        dependenciesBuilder.toEnv(),
        serverSettingsBuilder.toServerSettings()
    )

@PublishedApi
internal inline fun <reified M, reified S, J> ServerSettingsBuilder<M, S, J>.toServerSettings() =
    ServerSettings(
        id,
        jsonSerializer,
        url ?: localhost,
        sessionBuilder ?: ::WebSocketSession
    )
