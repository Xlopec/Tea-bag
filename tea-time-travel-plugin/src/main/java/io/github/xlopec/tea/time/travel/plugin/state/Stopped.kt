package io.github.xlopec.tea.time.travel.plugin.state

import io.github.xlopec.tea.time.travel.plugin.domain.Settings
import io.github.xlopec.tea.time.travel.plugin.domain.isValid

data class Stopped(
    override val settings: Settings
) : State {
    companion object

    val canStart: Boolean = settings.host.isValid() && settings.port.isValid()
}
