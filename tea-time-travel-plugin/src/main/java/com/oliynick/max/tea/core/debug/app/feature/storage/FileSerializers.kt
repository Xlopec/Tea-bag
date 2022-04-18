package com.oliynick.max.tea.core.debug.app.feature.storage

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.oliynick.max.tea.core.debug.app.domain.ComponentDebugState
import io.github.xlopec.tea.core.debug.protocol.ComponentId
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

internal suspend fun Gson.exportAll(
    file: File,
    sessions: Iterable<ComponentDebugState>
) {
    coroutineScope {
        sessions.forEach { debugState ->
            launch {
                export(file, debugState)
            }
        }
    }
}

internal suspend fun Gson.export(
    file: File,
    debugState: ComponentDebugState
) {
    withContext(IO) {
        BufferedWriter(FileWriter(file.generateFileName(debugState.id)))
            .use { bw ->
                toJson(debugState.toJsonObject(), bw)
            }
    }
}

suspend fun Gson.import(
    file: File
): ComponentDebugState = withContext(IO) {
    BufferedReader(FileReader(file))
        .use { br -> fromJson(br, JsonObject::class.java) }
        .toComponentDebugState()
}

internal fun File.generateFileName(
    id: ComponentId,
    timestamp: LocalDateTime = LocalDateTime.now()
) = File(this, "${id.value} session on ${DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(timestamp)}.json")
