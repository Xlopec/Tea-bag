package io.github.xlopec.tea.time.travel.plugin.feature.notification

import io.github.xlopec.tea.time.travel.plugin.integration.Command
import io.github.xlopec.tea.time.travel.plugin.integration.Message
import io.github.xlopec.tea.time.travel.plugin.integration.NotifyCommand
import io.github.xlopec.tea.time.travel.plugin.integration.PluginException
import io.github.xlopec.tea.time.travel.plugin.model.State
import io.github.xlopec.tea.time.travel.protocol.ComponentId
import java.io.File

data class DoNotifyOperationException(
    val exception: PluginException,
    val operation: Command?,
    val description: String?
) : NotifyCommand

data class DoWarnUnacceptableMessage(
    val message: Message,
    val state: State
) : NotifyCommand

data class DoNotifyComponentAttached(
    val id: ComponentId,
    val isComponentReattached: Boolean,
) : NotifyCommand

data class DoNotifyFileOperationSuccess(
    val title: String,
    val description: String,
    val forFile: File? = null,
) : NotifyCommand

data class DoNotifyFileOperationFailure(
    val title: String,
    val description: String,
    val forFile: File? = null,
) : NotifyCommand
