package io.github.xlopec.tea.time.travel.plugin.model.state

import io.github.xlopec.tea.time.travel.plugin.model.Settings

data class Stopping(
    override val settings: Settings
) : State
