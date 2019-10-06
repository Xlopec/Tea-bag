package com.oliynick.max.elm.time.travel.app.plugin

import io.ktor.server.engine.ApplicationEngine
import kotlinx.coroutines.channels.Channel
import java.util.concurrent.TimeUnit

class EngineManager(private val serverProvider: (settings: Settings, events: Channel<PluginMessage>) -> ApplicationEngine) {
    private var engine: ApplicationEngine? = null

    fun start(settings: Settings, events: Channel<PluginMessage>) {
        require(engine == null) { "server haven't been disposed" }

        serverProvider(settings, events).also { newEngine ->
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