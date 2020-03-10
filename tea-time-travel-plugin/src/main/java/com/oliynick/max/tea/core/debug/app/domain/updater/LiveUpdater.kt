@file:Suppress("FunctionName")

package com.oliynick.max.tea.core.debug.app.domain.updater

import com.oliynick.max.tea.core.component.UpdateWith
import com.oliynick.max.tea.core.component.command
import com.oliynick.max.tea.core.debug.app.domain.cms.DoWarnUnacceptableMessage
import com.oliynick.max.tea.core.debug.app.domain.cms.NotificationMessage
import com.oliynick.max.tea.core.debug.app.domain.cms.PluginCommand
import com.oliynick.max.tea.core.debug.app.domain.cms.PluginMessage
import com.oliynick.max.tea.core.debug.app.domain.cms.PluginState
import com.oliynick.max.tea.core.debug.app.domain.cms.UIMessage

fun <Env> LiveUpdater() where Env : NotificationUpdater,
                              Env : UiUpdater =
    object : LiveUpdater<Env> {}

interface LiveUpdater<Env> : Updater<Env> where Env : NotificationUpdater,
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

fun warnUnacceptableMessage(
    message: PluginMessage,
    state: PluginState
): UpdateWith<PluginState, PluginCommand> =
    state command DoWarnUnacceptableMessage(message, state)