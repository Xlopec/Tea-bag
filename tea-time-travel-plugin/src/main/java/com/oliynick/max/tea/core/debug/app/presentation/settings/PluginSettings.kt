package com.oliynick.max.tea.core.debug.app.presentation.settings

import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import com.oliynick.max.tea.core.debug.app.misc.PluginId
import com.oliynick.max.tea.core.debug.app.misc.isDetailedToStringEnabled
import com.oliynick.max.tea.core.debug.app.misc.properties
import com.oliynick.max.tea.core.debug.app.misc.settings
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
        project.properties.settings.isDetailedOutput != detailedToStringCheckBox.isSelected

    override fun getId(): String = PluginId

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
