package com.oliynick.max.tea.core.debug.app.feature.storage

import com.oliynick.max.tea.core.debug.app.StoreCommand
import com.oliynick.max.tea.core.debug.app.domain.ComponentDebugState
import com.oliynick.max.tea.core.debug.app.domain.Settings
import java.io.File

data class DoExportSessions(
    val dir: File,
    val sessions: Collection<ComponentDebugState>
): StoreCommand

@JvmInline
value class DoImportSession(
    val file: File
) : StoreCommand

@JvmInline
value class DoStoreSettings(
    val settings: Settings
) : StoreCommand
