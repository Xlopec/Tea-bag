package io.github.xlopec.tea.time.travel.plugin.model

import io.github.xlopec.tea.time.travel.plugin.feature.settings.Settings

data class Stopping(
    override val settings: Settings
) : State
