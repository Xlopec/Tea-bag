package io.github.xlopec.tea.time.travel.plugin.integration.data

import io.github.xlopec.tea.time.travel.plugin.feature.settings.Host
import io.github.xlopec.tea.time.travel.plugin.feature.settings.Port
import io.github.xlopec.tea.time.travel.plugin.feature.settings.Settings
import io.github.xlopec.tea.time.travel.plugin.model.Valid

val ValidSettings = Settings(
    host = Valid("localhost", Host.of("localhost")!!),
    port = Valid("8080", Port(8080)),
    isDetailedOutput = false
)
