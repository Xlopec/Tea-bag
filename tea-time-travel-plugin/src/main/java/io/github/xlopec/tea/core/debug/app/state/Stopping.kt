package io.github.xlopec.tea.core.debug.app.state

import io.github.xlopec.tea.core.debug.app.domain.Settings

data class Stopping(
    override val settings: Settings
) : State
