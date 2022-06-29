package io.github.xlopec.tea.time.travel.plugin.feature.storage

import arrow.core.Either
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.intellij.ide.util.PropertiesComponent
import io.github.xlopec.tea.core.ResolveCtx
import io.github.xlopec.tea.core.effects
import io.github.xlopec.tea.time.travel.plugin.feature.notification.ComponentImported
import io.github.xlopec.tea.time.travel.plugin.feature.notification.OperationException
import io.github.xlopec.tea.time.travel.plugin.integration.Message
import io.github.xlopec.tea.time.travel.plugin.integration.StoreCommand
import io.github.xlopec.tea.time.travel.plugin.integration.toPluginException
import io.github.xlopec.tea.time.travel.plugin.util.settings

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
            }.fold(::setOf, ::setOfNotNull)
        }
    }

    private suspend fun DoExportSessions.exportSettings(): Either<OperationException, Nothing?> =
        Either.catch { gson.exportAll(dir, sessions) }
            .map { null }
            .mapLeft { OperationException(it.toPluginException(), this, "couldn't export session to dir $dir") }

    private suspend fun DoImportSession.import(): Either<OperationException, ComponentImported> =
        gson.import(file)
            .bimap(
                {
                    OperationException(
                        it,
                        this,
                        "Couldn't import session from ${file.absolutePath}. Check if file is valid"
                    )
                },
                ::ComponentImported
            )

    private fun DoStoreSettings.storeSettings(): Either<OperationException, Nothing?> =
        Either.catch { properties.settings = settings }
            .map { null }
            .mapLeft { OperationException(it, this, "Couldn't store plugin settings") }
}
