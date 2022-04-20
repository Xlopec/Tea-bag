package io.github.xlopec.tea.time.travel.plugin.state

import io.github.xlopec.tea.time.travel.plugin.domain.Settings

data class Starting(
    override val settings: Settings
) : State
