package com.oliynick.max.tea.core.debug.app.component.cms.state

import com.oliynick.max.tea.core.debug.app.domain.Settings

data class Stopping(
    override val settings: Settings
) : State
