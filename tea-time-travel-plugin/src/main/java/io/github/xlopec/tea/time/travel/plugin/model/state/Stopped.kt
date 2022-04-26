package io.github.xlopec.tea.time.travel.plugin.model.state

import io.github.xlopec.tea.time.travel.plugin.model.Settings
import io.github.xlopec.tea.time.travel.plugin.model.isValid

data class Stopped(
    override val settings: Settings
) : State {
    companion object

    val canStart: Boolean = settings.host.isValid() && settings.port.isValid()
}
