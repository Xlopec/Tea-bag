package com.oliynick.max.elm.time.travel.app.transport

import com.oliynick.max.elm.time.travel.app.domain.PluginMessage
import com.oliynick.max.elm.time.travel.app.domain.Settings
import com.oliynick.max.elm.time.travel.protocol.Action
import com.oliynick.max.elm.time.travel.protocol.ComponentId
import io.ktor.server.engine.ApplicationEngine
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import java.util.concurrent.TimeUnit

class EngineManager(private val serverProvider: (settings: Settings,
                                                 events: BroadcastChannel<PluginMessage>,
                                                 outgoing: Channel<Pair<ComponentId, Action>>) -> ApplicationEngine) {
    private var engine: ApplicationEngine? = null

    val outgoing = Channel<Pair<ComponentId, Action>>()

    fun start(settings: Settings, events: BroadcastChannel<PluginMessage>) {
        require(engine == null) { "server haven't been disposed" }

        serverProvider(settings, events, outgoing).also { newEngine ->
            engine = newEngine
            newEngine.start()
        }
    }

    fun stop() {
        val old = engine
        engine = null

        requireNotNull(old) { "server haven't been started" }

        old.stop(1, 1, TimeUnit.SECONDS)
    }
}