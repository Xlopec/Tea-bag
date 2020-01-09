package com.oliynick.max.elm.time.travel.app.env

import com.oliynick.max.elm.core.actor.Component
import com.oliynick.max.elm.core.component.Component
import com.oliynick.max.elm.core.component.Env
import com.oliynick.max.elm.core.component.androidLogger
import com.oliynick.max.elm.time.travel.app.domain.cms.PluginCommand
import com.oliynick.max.elm.time.travel.app.domain.cms.PluginMessage
import com.oliynick.max.elm.time.travel.app.domain.cms.PluginState
import com.oliynick.max.elm.time.travel.app.domain.cms.Stopped
import com.oliynick.max.elm.time.travel.app.storage.pluginSettings

@Suppress("FunctionName")
fun Environment.PluginComponent(): Component<PluginMessage, PluginState> {

    suspend fun resolve(c: PluginCommand) = this.resolve(c)

    fun update(
        message: PluginMessage,
        state: PluginState
    ) = this.update(message, state)

    return Component(Env(
        Stopped(properties.pluginSettings),
        ::resolve,
        ::update,
        androidLogger("Plugin Component"))
    )

}