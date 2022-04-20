package io.github.xlopec.tea.time.travel.plugin.feature.storage

import io.github.xlopec.tea.core.debug.protocol.ComponentId
import io.github.xlopec.tea.time.travel.plugin.StoreMessage
import java.io.File

data class ExportSessions(
    val ids: Collection<ComponentId>,
    val dir: File
) : StoreMessage

@JvmInline
value class ImportSession(
    val file: File
) : StoreMessage
