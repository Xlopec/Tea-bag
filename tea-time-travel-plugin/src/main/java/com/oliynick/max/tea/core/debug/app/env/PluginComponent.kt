@file:Suppress("FunctionName")

package com.oliynick.max.tea.core.debug.app.env

import com.intellij.openapi.diagnostic.Logger
import com.oliynick.max.tea.core.Initial
import com.oliynick.max.tea.core.Initializer
import com.oliynick.max.tea.core.Regular
import com.oliynick.max.tea.core.Snapshot
import com.oliynick.max.tea.core.component.Component
import com.oliynick.max.tea.core.component.Interceptor
import com.oliynick.max.tea.core.component.with
import com.oliynick.max.tea.core.debug.app.domain.cms.PluginCommand
import com.oliynick.max.tea.core.debug.app.domain.cms.PluginMessage
import com.oliynick.max.tea.core.debug.app.domain.cms.PluginState
import com.oliynick.max.tea.core.debug.app.domain.cms.Stopped
import com.oliynick.max.tea.core.debug.app.storage.pluginSettings
import com.oliynick.max.tea.core.debug.app.transport.NewStoppedServer

fun PluginComponent(
    environment: Environment
): Component<PluginMessage, PluginState, PluginCommand> {

    suspend fun doResolve(
        c: PluginCommand
    ): Set<PluginMessage> = with(environment) { resolve(c) }

    fun doUpdate(
        message: PluginMessage,
        state: PluginState
    ) = with(environment) { update(message, state) }
    // fixme implement proper initializer properly
    return Component(Initializer(Stopped(environment.properties.pluginSettings, NewStoppedServer())), ::doResolve, ::doUpdate)
        .with(Logger())
}

private fun Logger(): Interceptor<PluginMessage, PluginState, PluginCommand> {
    val logger = Logger.getInstance("Tea-Bag-Plugin")

    return { snapshot -> logger.info(snapshot.formatted()) }
}

private fun Snapshot<*, *, *>.formatted(): String =
    when (this) {
        is Initial -> "Init with state=$currentState" +
                if (commands.isEmpty()) "" else ", commands=$commands"
        is Regular -> """
        Regular with new state=$currentState,
        prev state=$previousState,
        caused by message=$message
        ${if (commands.isEmpty()) "" else "\ncommands=$commands"}
    """.trimIndent()
    }
