package com.oliynick.max.tea.core.debug.app.component.cms.state

import com.oliynick.max.tea.core.debug.app.domain.Settings
import com.oliynick.max.tea.core.debug.app.domain.isValid

data class Stopped(
    override val settings: Settings
) : State {
    companion object

    val canStart: Boolean = settings.host.isValid() && settings.port.isValid()
}

fun Stopped.update(
    settings: Settings
) = copy(settings = settings)
