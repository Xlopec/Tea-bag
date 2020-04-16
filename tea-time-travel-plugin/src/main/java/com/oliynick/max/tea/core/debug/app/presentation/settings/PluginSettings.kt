package com.oliynick.max.tea.core.debug.app.presentation.settings

import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import com.oliynick.max.tea.core.debug.app.storage.PLUGIN_ID
import com.oliynick.max.tea.core.debug.app.storage.isDetailedToStringEnabled
import com.oliynick.max.tea.core.debug.app.storage.pluginSettings
import com.oliynick.max.tea.core.debug.app.storage.properties
import javax.swing.JCheckBox
import javax.swing.JComponent

class PluginSettings(
    private val project: Project
) : SearchableConfigurable {

    private lateinit var root: JComponent
    private lateinit var detailedToStringCheckBox: JCheckBox

    init {
        detailedToStringCheckBox.isSelected = project.properties.isDetailedToStringEnabled
    }

    override fun isModified(): Boolean =
        project.properties.pluginSettings.isDetailedOutput != detailedToStringCheckBox.isSelected

    override fun getId(): String = PLUGIN_ID

    override fun getDisplayName(): String = "Time Travel"

    override fun apply() {
        project.messageBus.syncPublisher(PluginSettingsNotifier.TOPIC)
            .onSettingsUpdated(detailedToStringCheckBox.isSelected)
    }

    override fun reset() {
        detailedToStringCheckBox.isSelected = project.properties.isDetailedToStringEnabled
    }

    override fun createComponent(): JComponent = root

    override fun enableSearch(option: String?): Runnable? = null

}
