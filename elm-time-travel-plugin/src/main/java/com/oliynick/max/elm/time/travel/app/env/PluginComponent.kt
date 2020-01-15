package com.oliynick.max.elm.time.travel.app.env

import com.oliynick.max.elm.core.actor.ComponentLegacy
import com.oliynick.max.elm.core.component.ComponentLegacy
import com.oliynick.max.elm.core.component.Env
import com.oliynick.max.elm.core.component.androidLogger
import com.oliynick.max.elm.time.travel.app.domain.cms.PluginCommand
import com.oliynick.max.elm.time.travel.app.domain.cms.PluginMessage
import com.oliynick.max.elm.time.travel.app.domain.cms.PluginState
import com.oliynick.max.elm.time.travel.app.domain.cms.Stopped
import com.oliynick.max.elm.time.travel.app.storage.pluginSettings

@Suppress("FunctionName")
fun Environment.PluginComponent(): ComponentLegacy<PluginMessage, PluginState> {

    suspend fun resolve(c: PluginCommand) = this.resolve(c)

    fun update(
        message: PluginMessage,
        state: PluginState
    ) = this.update(message, state)

    return ComponentLegacy(Env(
        Stopped(properties.pluginSettings),
        ::resolve,
        ::update,
        androidLogger("Plugin Component"))
    )

}