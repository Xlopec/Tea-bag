@file:Suppress("FunctionName")
@file:OptIn(UnstableApi::class)

package com.oliynick.max.tea.core.debug.component

import com.oliynick.max.tea.core.*
import com.oliynick.max.tea.core.debug.session.*
import protocol.ComponentId
import protocol.JsonConverter
import java.net.URL

/**
 * Same as [environment][Env] but with extra settings
 *
 * @param componentEnv see [environment][Env]
 * @param serverSettings server settings to use
 * @param M message type
 * @param S state type
 * @param C command type
 * @param J json tree type
 */
data class DebugEnv<M, S, C, J>(
    inline val componentEnv: Env<M, S, C>,
    inline val serverSettings: ServerSettings<M, S, J>
)

/**
 * Holds server settings such as component identifier,
 * json serializer, debug server url and so on
 *
 * @param id component identifier
 * @param url debug server url
 * @param serializer json serializer
 * @param sessionBuilder a function that for a given server settings creates a new [debug session][DebugSession]
 * @param M message type
 * @param S state type
 * @param J json tree type
 */
data class ServerSettings<M, S, J>(
    val id: ComponentId,
    val serializer: JsonConverter<J>,
    val url: URL,
    val sessionBuilder: SessionBuilder<M, S, J>
)

/**
 * Configures and returns debug dependencies
 *
 * @param id component identifier
 * @param env debug environment
 * @param jsonConverter json serializer
 * @param config configuration block
 * @param M message type
 * @param S state type
 * @param C command type
 * @param J json tree type
 */
inline fun <reified M, reified C, reified S, J> Dependencies(
    id: ComponentId,
    env: EnvBuilder<M, S, C>,
    jsonConverter: JsonConverter<J>,
    config: DebugEnvBuilder<M, S, C, J>.() -> Unit = {}
) = DebugEnvBuilder(
    env,
    ServerSettingsBuilder(id, jsonConverter, ::WebSocketSession)
).apply(config).toDebugDependencies()

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

@DslMarker
private annotation class DslBuilder
