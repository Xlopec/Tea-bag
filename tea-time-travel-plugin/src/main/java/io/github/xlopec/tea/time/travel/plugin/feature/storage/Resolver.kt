package io.github.xlopec.tea.time.travel.plugin.feature.storage

import arrow.core.Either
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.intellij.ide.util.PropertiesComponent
import io.github.xlopec.tea.core.ResolveCtx
import io.github.xlopec.tea.core.effects
import io.github.xlopec.tea.time.travel.plugin.feature.notification.ComponentExportResult
import io.github.xlopec.tea.time.travel.plugin.feature.notification.ComponentImportResult
import io.github.xlopec.tea.time.travel.plugin.feature.notification.OperationException
import io.github.xlopec.tea.time.travel.plugin.integration.InternalException
import io.github.xlopec.tea.time.travel.plugin.integration.Message
import io.github.xlopec.tea.time.travel.plugin.integration.StoreCommand
import io.github.xlopec.tea.time.travel.plugin.util.settings
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

fun interface StorageResolver {
    fun resolveStoreCommand(
        command: StoreCommand,
        ctx: ResolveCtx<Message>,
    )
}

fun StorageResolver(
    properties: PropertiesComponent
): StorageResolver = StorageResolverImpl(properties)

private class StorageResolverImpl(
    private val properties: PropertiesComponent,
    private val gson: Gson = GsonBuilder().serializeNulls().create(),
) : StorageResolver {

    override fun resolveStoreCommand(
        command: StoreCommand,
        ctx: ResolveCtx<Message>,
    ) {
        ctx.effects {
            when (command) {
                is DoExportSessions -> command.exportSettings()
                is DoImportSession -> command.import()
                is DoStoreSettings -> command.storeSettings()
                else -> error("can't get here")
            }
        }
    }

    private suspend fun DoExportSessions.exportSettings(): Set<ComponentExportResult> = coroutineScope {
        sessions.map { debugState -> async { gson.export(dir.generateFileNameForExport(debugState.id), debugState) } }
            .mapTo(mutableSetOf()) { it.await() }
    }

    private suspend fun DoImportSession.import(): Set<ComponentImportResult> = setOf(gson.import(file))

    private fun DoStoreSettings.storeSettings(): Set<OperationException> =
        Either.catch { properties.settings = settings }
            .mapLeft { OperationException(InternalException("Plugin couldn't store settings", it), this) }
            .fold({ it }, { null })
            .let(::setOfNotNull)
}
