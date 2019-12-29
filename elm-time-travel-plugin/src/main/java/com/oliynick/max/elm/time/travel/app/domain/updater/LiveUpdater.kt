@file:Suppress("FunctionName")

package com.oliynick.max.elm.time.travel.app.domain.updater

import com.oliynick.max.elm.core.component.UpdateWith
import com.oliynick.max.elm.time.travel.app.domain.cms.*

fun <Env> LiveUpdater() where Env : NotificationUpdater,
                              Env : UiUpdater =
    object : LiveUpdater<Env> {}

interface LiveUpdater<Env> :
    Updater<Env> where Env : NotificationUpdater,
                       Env : UiUpdater {

    override fun Env.update(
        message: PluginMessage,
        state: PluginState
    ): UpdateWith<PluginState, PluginCommand> =
        when (message) {
            is UIMessage -> update(message, state)
            is NotificationMessage -> update(message, state)
        }

}
