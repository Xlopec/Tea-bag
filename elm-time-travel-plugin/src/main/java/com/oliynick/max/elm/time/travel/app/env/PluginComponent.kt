@file:Suppress("FunctionName")

package com.oliynick.max.elm.time.travel.app.env

import com.oliynick.max.elm.core.component.*
import com.oliynick.max.elm.core.loop.ComponentFock
import com.oliynick.max.elm.time.travel.app.domain.cms.PluginCommand
import com.oliynick.max.elm.time.travel.app.domain.cms.PluginMessage
import com.oliynick.max.elm.time.travel.app.domain.cms.PluginState
import com.oliynick.max.elm.time.travel.app.domain.cms.Stopped
import com.oliynick.max.elm.time.travel.app.storage.pluginSettings
import kotlinx.coroutines.flow.Flow

fun Environment.PluginComponent(): (Flow<PluginMessage>) -> Flow<PluginState> {

    suspend fun resolve(c: PluginCommand) = this.resolve(c)

    fun update(
        message: PluginMessage,
        state: PluginState
    ) = this.update(message, state)

    return ComponentFock(Initializer(Stopped(properties.pluginSettings)), ::resolve, ::update).with(Logger()).states()
}

private fun Logger(): Interceptor<PluginMessage, PluginState, PluginCommand> =
    { snapshot -> println(format(snapshot)) }

private fun <M, C, S> format(
    snapshot: Snapshot<M, C, S>
): String = when(snapshot) {
    is Initial -> """
        Init with state=${snapshot.state} ${snapshot.state.hashCode()}
        ${if (snapshot.commands.isEmpty()) "" else "commands=${snapshot.commands}"}
    """.trimIndent()
    is Regular -> """
        Regular with state=${snapshot.state} ${snapshot.state.hashCode()}
        message=${snapshot.message}
        ${if (snapshot.commands.isEmpty()) "" else "commands=${snapshot.commands}"}
    """.trimIndent()
}
