@file:Suppress("FunctionName")

package io.github.xlopec.tea.time.travel.plugin.feature.notification

import com.intellij.notification.NotificationType
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import io.github.xlopec.tea.time.travel.plugin.integration.Command
import io.github.xlopec.tea.time.travel.plugin.integration.NotifyCommand
import io.github.xlopec.tea.time.travel.plugin.integration.PluginException
import io.github.xlopec.tea.time.travel.plugin.util.Action
import java.io.File
import java.util.*

fun interface NotificationResolver {
    fun resolve(
        command: NotifyCommand
    )
}

fun NotificationResolver(
    project: Project
): NotificationResolver = NotificationResolverImpl(project)

private class NotificationResolverImpl(
    private val project: Project
) : NotificationResolver {

    override fun resolve(
        command: NotifyCommand
    ) {
        when (command) {
            is DoNotifyOperationException -> command.showOperationException()
            is DoWarnUnacceptableMessage -> command.showUnacceptableMessageWarning()
            is DoNotifyComponentAttached -> command.showComponentAttachedNotification()
            is DoNotifyFileOperationSuccess -> command.showFileOperationSuccess()
            is DoNotifyFileOperationFailure -> command.showFileOperationFailure()
            else -> error("can't get here")
        }
    }

    private fun DoNotifyComponentAttached.showComponentAttachedNotification() {
        project.showNotification(
            "New Client Attached",
            "Component \"${id.value}\" ${"re".takeIf { isComponentReattached } ?: ""}attached",
            NotificationType.INFORMATION
        )
    }

    private fun DoNotifyFileOperationSuccess.showFileOperationSuccess() {
        val openFileAction = forFile?.toVirtualFile()?.let { file -> OpenFileAction(file, project) }

        project.showNotification(
            title,
            description,
            NotificationType.INFORMATION,
            listOfNotNull(openFileAction)
        )
    }

    private fun DoNotifyFileOperationFailure.showFileOperationFailure() {
        val openFileAction = forFile?.toVirtualFile()?.let { file -> OpenFileAction(file, project) }

        project.showNotification(
            title,
            description,
            NotificationType.ERROR,
            listOfNotNull(openFileAction)
        )
    }

    private fun DoWarnUnacceptableMessage.showUnacceptableMessageWarning() {
        project.showNotification(
            "Tea Time Travel Plugin Exception",
            "Message ${message.javaClass.simpleName} can't be applied to state ${state.javaClass.simpleName}",
            NotificationType.WARNING
        )
    }

    private fun DoNotifyOperationException.showOperationException() {
        project.showNotification(
            "Tea Time Travel Plugin Exception",
            exceptionDescription(exception, operation, description),
            NotificationType.ERROR
        )
    }
}

private fun exceptionDescription(
    cause: PluginException,
    operation: Command?,
    description: String?,
): String = "Exception occurred: ${formattedCauseDescription(cause, operation, description)}"

private fun formattedCauseDescription(
    cause: PluginException,
    operation: Command?,
    description: String?
) = (description ?: cause.message ?: operation?.javaClass?.simpleName ?: "unknown exception")
    .replaceFirstChar { it.lowercase(Locale.ENGLISH) }

private fun File.toVirtualFile(): VirtualFile? = VirtualFileManager.getInstance().findFileByNioPath(toPath())

private fun OpenFileAction(
    file: VirtualFile,
    project: Project
) = Action("Open File") {
    FileEditorManager.getInstance(project).openFile(file, true)
}
