package io.github.xlopec.tea.time.travel.plugin.feature.storage

import com.google.gson.Gson
import com.google.gson.JsonObject
import io.github.xlopec.tea.time.travel.plugin.model.DebuggableComponent
import io.github.xlopec.tea.time.travel.plugin.util.toJson
import io.github.xlopec.tea.time.travel.protocol.ComponentId
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal suspend fun Gson.exportAll(
    file: File,
    sessions: Iterable<DebuggableComponent>
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
    debugState: DebuggableComponent
) = toJson(debugState.toJsonObject(), file.generateFileName(debugState.id))

suspend fun Gson.import(
    file: File
): DebuggableComponent = withContext(IO) {
    BufferedReader(FileReader(file))
        .use { br -> fromJson(br, JsonObject::class.java) }
        .toComponentDebugState()
}

internal fun File.generateFileName(
    id: ComponentId,
    timestamp: LocalDateTime = LocalDateTime.now()
) = File(this, "${id.value} session on ${DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(timestamp)}.json")
