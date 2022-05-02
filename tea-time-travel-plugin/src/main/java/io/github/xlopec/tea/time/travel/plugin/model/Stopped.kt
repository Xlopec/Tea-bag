package io.github.xlopec.tea.time.travel.plugin.model

import io.github.xlopec.tea.time.travel.plugin.feature.settings.Settings

data class Stopped(
    override val settings: Settings
) : State {
    companion object

    val canStart: Boolean = settings.host.isValid() && settings.port.isValid()
}
