/*
 * Copyright (C) 2021. Maksym Oliinyk.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.xlopec.tea.core.debug.app.feature.presentation.ui.settings

import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import io.github.xlopec.tea.core.debug.app.misc.PluginId
import io.github.xlopec.tea.core.debug.app.misc.isDetailedToStringEnabled
import io.github.xlopec.tea.core.debug.app.misc.properties
import io.github.xlopec.tea.core.debug.app.misc.settings
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
