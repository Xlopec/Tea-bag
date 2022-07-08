package io.github.xlopec.tea.time.travel.plugin.feature.storage

import arrow.core.Either
import com.google.gson.Gson
import com.google.gson.JsonObject
import io.github.xlopec.tea.time.travel.plugin.integration.FileException
import io.github.xlopec.tea.time.travel.plugin.model.DebuggableComponent
import io.github.xlopec.tea.time.travel.plugin.util.toJson
import io.github.xlopec.tea.time.travel.protocol.ComponentId
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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

internal suspend fun Gson.import(
    file: File
): Either<FileException, DebuggableComponent> =
    Either.catch {
        withContext(IO) {
            BufferedReader(FileReader(file))
                .use { br -> fromJson(br, JsonObject::class.java) }
                .toComponentDebugState()
        }
    }.mapLeft { FileException("Couldn't import session from ${file.absolutePath}. Check if file is valid", it, file) }

internal fun File.generateFileName(
    id: ComponentId,
    timestamp: LocalDateTime = LocalDateTime.now()
) = File(this, "${id.value} session on ${DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(timestamp)}.json")
