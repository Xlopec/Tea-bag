package com.oliynick.max.elm.time.travel.component

import com.oliynick.max.elm.core.component.Env
import com.oliynick.max.elm.core.component.EnvBuilder
import com.oliynick.max.elm.time.travel.converter.GsonSerializer
import com.oliynick.max.elm.time.travel.converter.JsonConverter
import protocol.ComponentId
import java.net.URL

@DslMarker
private annotation class DslBuilder

//todo add dsl
data class DebugEnv<M, C, S>(
    inline val componentEnv: Env<M, C, S>,
    inline val serverSettings: ServerSettings,
    val sessionBuilder: SessionBuilder<M, S>
)

//todo add dsl
data class ServerSettings(
    inline val id: ComponentId,
    inline val serializer: JsonConverter,
    inline val url: URL
)

@DslBuilder
class ServerSettingsBuilder internal constructor(
    var id: ComponentId,
    var url: URL,
    var jsonSerializer: JsonConverter
) {

    fun installSerializer(
        serializer: JsonConverter
    ) {
        jsonSerializer = serializer
    }

}

@DslBuilder
class DebugEnvBuilder<M, C, S> internal constructor(
    var dependenciesBuilder: EnvBuilder<M, C, S>,
    var serverSettingsBuilder: ServerSettingsBuilder
) {

    fun dependencies(config: EnvBuilder<M, C, S>.() -> Unit) {
        dependenciesBuilder.apply(config)
    }

    fun serverSettings(config: ServerSettingsBuilder.() -> Unit) {
        serverSettingsBuilder.apply(config)
    }

}

fun <M, C, S> Dependencies(
    id: ComponentId,
    env: Env<M, C, S>,
    url: URL = localhost,
    serializer: JsonConverter = GsonSerializer(),
    config: DebugEnvBuilder<M, C, S>.() -> Unit = {}
) = DebugEnvBuilder(
    EnvBuilder(env),
    ServerSettingsBuilder(id, url, serializer)
).apply(config).toDebugDependencies()