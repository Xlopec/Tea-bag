package io.github.xlopec.tea.time.travel.plugin.feature.storage

import io.github.xlopec.tea.time.travel.plugin.integration.StoreCommand
import io.github.xlopec.tea.time.travel.plugin.feature.component.model.ComponentState
import io.github.xlopec.tea.time.travel.plugin.feature.settings.Settings
import java.io.File

data class DoExportSessions(
    val dir: File,
    val sessions: Collection<ComponentState>
) : StoreCommand

@JvmInline
value class DoImportSession(
    val file: File
) : StoreCommand

@JvmInline
value class DoStoreSettings(
    val settings: Settings
) : StoreCommand
