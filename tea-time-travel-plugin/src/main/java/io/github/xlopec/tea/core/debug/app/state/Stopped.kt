package io.github.xlopec.tea.core.debug.app.state

import io.github.xlopec.tea.core.debug.app.domain.Settings
import io.github.xlopec.tea.core.debug.app.domain.isValid

data class Stopped(
    override val settings: Settings
) : State {
    companion object

    val canStart: Boolean = settings.host.isValid() && settings.port.isValid()
}
