package com.oliynick.max.elm.time.travel.component

import com.oliynick.max.elm.core.component.Env
import com.oliynick.max.elm.core.component.EnvBuilder
import com.oliynick.max.elm.time.travel.converter.JsonConverter
import com.oliynick.max.elm.time.travel.session.SessionBuilder
import protocol.ComponentId
import java.net.URL

@DslMarker
private annotation class DslBuilder

//todo add dsl
data class DebugEnv<M, C, S>(
    inline val componentEnv: Env<M, C, S>,
    inline val serverSettings: ServerSettings<M, S>
)

//todo add dsl
data class ServerSettings<M, S>(
    inline val id: ComponentId,
    inline val serializer: JsonConverter,
    inline val url: URL,
    inline val sessionBuilder: SessionBuilder<M, S>
)

@DslBuilder
class ServerSettingsBuilder<M, S> @PublishedApi internal constructor(
    val id: ComponentId
) {

    internal var url: URL? = null
    internal var jsonSerializer: JsonConverter? = null
    internal var sessionBuilder: SessionBuilder<M, S>? = null

    fun url(
        u: URL
    ) {
        url = u
    }

    fun installSessionBuilder(
        builder: SessionBuilder<M, S>
    ) {
        sessionBuilder = builder
    }

    fun installSerializer(
        serializer: JsonConverter
    ) {
        jsonSerializer = serializer
    }

}

@DslBuilder
class DebugEnvBuilder<M, C, S> @PublishedApi internal constructor(
    var dependenciesBuilder: EnvBuilder<M, C, S>,
    var serverSettingsBuilder: ServerSettingsBuilder<M, S>
) {

    fun dependencies(config: EnvBuilder<M, C, S>.() -> Unit) {
        dependenciesBuilder.apply(config)
    }

    fun serverSettings(config: ServerSettingsBuilder<M, S>.() -> Unit) {
        serverSettingsBuilder.apply(config)
    }

}

inline fun <reified M, reified C, reified S> Dependencies(
    id: ComponentId,
    env: Env<M, C, S>,
    config: DebugEnvBuilder<M, C, S>.() -> Unit = {}
) = DebugEnvBuilder(
    EnvBuilder(env),
    ServerSettingsBuilder(id)
).apply(config).toDebugDependencies()