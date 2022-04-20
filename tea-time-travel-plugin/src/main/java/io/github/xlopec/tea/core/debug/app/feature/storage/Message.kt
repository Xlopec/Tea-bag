package io.github.xlopec.tea.core.debug.app.feature.storage

import io.github.xlopec.tea.core.debug.app.StoreMessage
import io.github.xlopec.tea.core.debug.protocol.ComponentId
import java.io.File

data class ExportSessions(
    val ids: Collection<ComponentId>,
    val dir: File
) : StoreMessage

@JvmInline
value class ImportSession(
    val file: File
) : StoreMessage