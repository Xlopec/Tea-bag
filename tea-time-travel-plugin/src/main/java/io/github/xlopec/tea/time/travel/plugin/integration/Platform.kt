package io.github.xlopec.tea.time.travel.plugin.integration

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiClass
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.PsiNavigateUtil
import io.github.xlopec.tea.time.travel.plugin.feature.settings.PluginSettings
import io.github.xlopec.tea.time.travel.plugin.feature.storage.ExportSessions
import io.github.xlopec.tea.time.travel.plugin.model.Type
import io.github.xlopec.tea.time.travel.plugin.util.chooseFile
import io.github.xlopec.tea.time.travel.plugin.util.javaPsiFacade
import io.github.xlopec.tea.time.travel.protocol.ComponentId
import java.io.File
import kotlinx.collections.immutable.ImmutableSet

interface Platform {

    suspend fun chooseSessionFile(): File

    suspend fun chooseExportSessionDirectory(
        ids: ImmutableSet<ComponentId>,
    ): ExportSessions?

    fun psiClassFor(
        type: Type
    ): PsiClass?

    fun navigateToSources(
        psiClass: PsiClass
    )

    fun navigateToSettings()
}

fun Platform(
    project: Project,
    logger: Logger,
): Platform = PlatformImpl(project, logger)

private class PlatformImpl(
    private val project: Project,
    private val logger: Logger,
) : Platform {

    override suspend fun chooseSessionFile(): File =
        project.chooseFile(
            FileChooserDescriptorFactory.createSingleFileDescriptor("json")
                .withRoots(listOfNotNull(project.baseVirtualDir))
                .withTitle("Choose Session to Import")
        )

    override suspend fun chooseExportSessionDirectory(ids: ImmutableSet<ComponentId>): ExportSessions? {
        // if there is no ambiguity regarding what session we should store - don't show chooser popup
        val exportSelection = if (ids.size > 1) chooseComponentsForExport(ids.toList()) else ids

        return if (exportSelection.isNotEmpty()) {
            ExportSessions(exportSelection, chooseExportSessionDir())
        } else {
            null
        }
    }

    override fun psiClassFor(type: Type): PsiClass? =
        // findClass throws exception when indexing is in progress
        runCatching { project.javaPsiFacade.findClass(type.name, GlobalSearchScope.projectScope(project)) }
            .onFailure { logger.warn("Exception when searching for psi class for $type", it) }
            .getOrNull()

    override fun navigateToSources(psiClass: PsiClass) = PsiNavigateUtil.navigate(psiClass)
    override fun navigateToSettings() =
        ShowSettingsUtil.getInstance().showSettingsDialog(project, PluginSettings::class.java)

    private val Project.baseVirtualDir: VirtualFile?
        get() = basePath?.let(LocalFileSystem.getInstance()::findFileByPath)

    private suspend fun chooseExportSessionDir() =
        project.chooseFile(
            FileChooserDescriptorFactory.createSingleFolderDescriptor()
                .withFileFilter(VirtualFile::isDirectory)
                .withRoots(listOfNotNull(project.baseVirtualDir))
                .withTitle("Choose Directory to Save Session")
        )

    private fun chooseComponentsForExport(
        sessionIds: List<ComponentId>,
    ): List<ComponentId> {
        val option = Messages.showChooseDialog(
            project,
            "Select which session to export",
            "Export Session",
            null,
            arrayOf("All", *sessionIds.map(ComponentId::value).toTypedArray()),
            "All"
        )

        return when (option) {
            // cancel
            -1 -> listOf()
            // all
            0 -> sessionIds
            else -> sessionIds.subList(option - 1, option)
        }
    }
}
