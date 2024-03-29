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

package io.github.xlopec.tea.time.travel.plugin.feature.settings

import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import io.github.xlopec.tea.time.travel.plugin.model.PInt
import io.github.xlopec.tea.time.travel.plugin.model.toInt
import io.github.xlopec.tea.time.travel.plugin.model.toPInt
import io.github.xlopec.tea.time.travel.plugin.util.*
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel

class PluginSettings(
    private val project: Project
) : SearchableConfigurable {

    private lateinit var root: JComponent
    private lateinit var detailedToStringCheckBox: JCheckBox
    private lateinit var clearComponentSnapshotsOnAttachCheckBox: JCheckBox
    private lateinit var maxNumberOfSnapshots: JSpinner

    init {
        with(project.properties) {
            detailedToStringCheckBox.isSelected = isDetailedToStringEnabled
            clearComponentSnapshotsOnAttachCheckBox.isSelected = clearSnapshotsOnComponentAttach
            maxNumberOfSnapshots.model =
                SpinnerNumberModel(maxSnapshots.toInt(), PInt.MIN_VALUE.toInt(), Int.MAX_VALUE, PInt.MIN_VALUE.toInt())
            maxNumberOfSnapshots.value = maxSnapshots.toInt()
        }
    }

    override fun isModified(): Boolean = with(project.properties) {
        isDetailedToStringEnabled != detailedToStringCheckBox.isSelected ||
                clearSnapshotsOnComponentAttach != clearComponentSnapshotsOnAttachCheckBox.isSelected ||
                maxSnapshots != maxNumberOfSnapshots.value
    }

    override fun getId(): String = PluginId

    override fun getDisplayName(): String = "Time Travel"

    override fun apply() {
        project.messageBus.syncPublisher(PluginSettingsNotifier.TOPIC)
            .onSettingsUpdated(
                detailedToStringCheckBox.isSelected,
                clearComponentSnapshotsOnAttachCheckBox.isSelected,
                (maxNumberOfSnapshots.value as Int).toPInt()
            )
    }

    override fun reset() {
        with(project.properties) {
            detailedToStringCheckBox.isSelected = isDetailedToStringEnabled
            clearComponentSnapshotsOnAttachCheckBox.isSelected = clearSnapshotsOnComponentAttach
            maxNumberOfSnapshots.value = maxSnapshots.toInt()
        }
    }

    override fun createComponent(): JComponent = root

    override fun enableSearch(option: String?): Runnable? = null
}
