package io.github.xlopec.tea.time.travel.plugin.feature.storage

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.intellij.ide.util.PropertiesComponent
import io.github.xlopec.tea.data.Either
import io.github.xlopec.tea.data.Left
import io.github.xlopec.tea.time.travel.plugin.NotificationMessage
import io.github.xlopec.tea.time.travel.plugin.StoreCommand
import io.github.xlopec.tea.time.travel.plugin.feature.notification.ComponentImported
import io.github.xlopec.tea.time.travel.plugin.feature.notification.OperationException
import io.github.xlopec.tea.time.travel.plugin.misc.settings
import io.github.xlopec.tea.time.travel.plugin.toPluginException

fun interface StorageResolver {
    suspend fun resolveStoreCommand(
        command: StoreCommand
    ): Either<NotificationMessage?, OperationException>
}

fun StorageResolver(
    properties: PropertiesComponent
): StorageResolver = StorageResolverImpl(properties)

private class StorageResolverImpl(
    private val properties: PropertiesComponent,
    private val gson: Gson = GsonBuilder().serializeNulls().create(),
) : StorageResolver {

    override suspend fun resolveStoreCommand(
        command: StoreCommand
    ): Either<NotificationMessage?, OperationException> =
        when (command) {
            is DoExportSessions -> command.exportSettings()
            is DoImportSession -> command.import()
            is DoStoreSettings -> command.storeSettings()
            else -> error("can't get here")
        }

    suspend fun DoExportSessions.exportSettings() = Either(
        { gson.exportAll(dir, sessions); null },
        { OperationException(it.toPluginException(), this, "couldn't export session to dir $dir") }
    )

    suspend fun DoImportSession.import() = Either(
        { ComponentImported(gson.import(file)) },
        {
            OperationException(
                it.toPluginException(),
                this,
                "couldn't import session from ${file.absolutePath}. Check if file is valid"
            )
        }
    )

    private fun DoStoreSettings.storeSettings() =
        Left { properties.settings = settings }
}
