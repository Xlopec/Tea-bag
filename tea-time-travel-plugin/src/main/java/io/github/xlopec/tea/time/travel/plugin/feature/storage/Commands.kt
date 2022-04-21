package io.github.xlopec.tea.time.travel.plugin.feature.storage

import io.github.xlopec.tea.time.travel.plugin.StoreCommand
import io.github.xlopec.tea.time.travel.plugin.domain.ComponentDebugState
import io.github.xlopec.tea.time.travel.plugin.domain.Settings
import java.io.File

data class DoExportSessions(
    val dir: File,
    val sessions: Collection<ComponentDebugState>
) : StoreCommand

@JvmInline
value class DoImportSession(
    val file: File
) : StoreCommand

@JvmInline
value class DoStoreSettings(
    val settings: Settings
) : StoreCommand
