package com.oliynick.max.tea.core.debug.app.state

import com.oliynick.max.tea.core.debug.app.domain.Settings

data class Stopping(
    override val settings: Settings
) : State