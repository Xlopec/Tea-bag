@file:Suppress("FunctionName")

package com.oliynick.max.tea.core.debug.app.env

import com.oliynick.max.tea.core.Initial
import com.oliynick.max.tea.core.Initializer
import com.oliynick.max.tea.core.Regular
import com.oliynick.max.tea.core.Snapshot
import com.oliynick.max.tea.core.component.Component
import com.oliynick.max.tea.core.component.Interceptor
import com.oliynick.max.tea.core.component.states
import com.oliynick.max.tea.core.component.with
import com.oliynick.max.tea.core.debug.app.domain.cms.PluginCommand
import com.oliynick.max.tea.core.debug.app.domain.cms.PluginMessage
import com.oliynick.max.tea.core.debug.app.domain.cms.PluginState
import com.oliynick.max.tea.core.debug.app.domain.cms.Stopped
import com.oliynick.max.tea.core.debug.app.storage.pluginSettings
import kotlinx.coroutines.flow.Flow

fun Environment.PluginComponent(): (Flow<PluginMessage>) -> Flow<PluginState> {

    suspend fun resolve(c: PluginCommand) = this.resolve(c)

    fun update(
        message: PluginMessage,
        state: PluginState
    ) = this.update(message, state)

    return Component(
        Initializer(
            Stopped(
                properties.pluginSettings
            )
        ), ::resolve, ::update
    ).with(Logger()).states()
}

private fun Logger(): Interceptor<PluginMessage, PluginState, PluginCommand> =
    { snapshot -> println(format(snapshot)) }

private fun <M, S, C> format(
    snapshot: Snapshot<M, S, C>
): String = when (snapshot) {
    is Initial -> """
        Init with state=${snapshot.currentState} ${snapshot.currentState.hashCode()}
        ${if (snapshot.commands.isEmpty()) "" else "commands=${snapshot.commands}"}
    """.trimIndent()
    is Regular -> """
        Regular with state=${snapshot.currentState} ${snapshot.currentState.hashCode()}
        message=${snapshot.message}
        ${if (snapshot.commands.isEmpty()) "" else "commands=${snapshot.commands}"}
    """.trimIndent()
}
