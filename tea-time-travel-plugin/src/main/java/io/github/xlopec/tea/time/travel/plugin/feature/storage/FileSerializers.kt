package io.github.xlopec.tea.time.travel.plugin.feature.storage

import arrow.core.Either
import com.google.gson.Gson
import com.google.gson.JsonObject
import io.github.xlopec.tea.time.travel.plugin.feature.notification.*
import io.github.xlopec.tea.time.travel.plugin.integration.FileException
import io.github.xlopec.tea.time.travel.plugin.model.DebuggableComponent
import io.github.xlopec.tea.time.travel.plugin.util.foldSuper
import io.github.xlopec.tea.time.travel.plugin.util.toJson
import io.github.xlopec.tea.time.travel.protocol.ComponentId
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

internal suspend fun Gson.export(
    file: File,
    debugState: DebuggableComponent
): ComponentExportResult =
    withContext(IO) {
        Either.catch {
            toJson(debugState.toJsonObject(), file)
            ComponentExportSuccess(debugState.id, file)
        }.mapLeft {
            ComponentExportFailure(debugState.id, FileException("Couldn't export session to file $file", it, file))
        }.foldSuper()
    }

internal suspend fun Gson.import(
    file: File
): ComponentImportResult =
    withContext(IO) {
        Either.catch {
            ComponentImportSuccess(
                file,
                BufferedReader(FileReader(file))
                .use { br -> fromJson(br, JsonObject::class.java) }
                .toComponentDebugState()
            )
        }.mapLeft {
            ComponentImportFailure(FileException("Couldn't import session from ${file.absolutePath}", it, file))
        }.foldSuper()
    }

internal fun File.generateFileNameForExport(
    id: ComponentId,
    timestamp: LocalDateTime = LocalDateTime.now()
) = File(this, "${id.value} session on ${DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(timestamp)}.json")
