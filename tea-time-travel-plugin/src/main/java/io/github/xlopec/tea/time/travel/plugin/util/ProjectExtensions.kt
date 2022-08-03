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

package io.github.xlopec.tea.time.travel.plugin.util

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import io.github.xlopec.tea.time.travel.plugin.feature.settings.Settings
import io.github.xlopec.tea.time.travel.plugin.feature.settings.Settings.Companion.DefaultMaxSnapshots
import io.github.xlopec.tea.time.travel.plugin.model.PositiveNumber
import io.github.xlopec.tea.time.travel.plugin.model.toInt
import io.github.xlopec.tea.time.travel.plugin.model.toUInt

const val PluginId = "io.github.xlopec.tea.core.plugin"
private const val DetailedToStringKey = "$PluginId.isDetailedToStringEnabled"
private const val ClearLogsKey = "$PluginId.clearLogsOnComponentAttach"
private const val HostKey = "$PluginId.host"
private const val PortKey = "$PluginId.port"
private const val MaxSnapshotsKey = "$PluginId.maxSnapshots"

val Project.properties: PropertiesComponent
    get() = PropertiesComponent.getInstance(this)

var PropertiesComponent.settings: Settings
    set(value) {
        host = value.host.input
        port = value.port.input
        isDetailedToStringEnabled = value.isDetailedOutput
        clearSnapshotsOnComponentAttach = value.clearSnapshotsOnAttach
        maxSnapshots = value.maxSnapshots
    }
    get() = Settings.fromInput(host, port, isDetailedToStringEnabled, clearSnapshotsOnComponentAttach, maxSnapshots.toUInt())

// todo reduce visibility
var PropertiesComponent.isDetailedToStringEnabled: Boolean
    set(value) = setValue(DetailedToStringKey, value)
    get() = getBoolean(DetailedToStringKey, false)

var PropertiesComponent.clearSnapshotsOnComponentAttach: Boolean
    set(value) = setValue(ClearLogsKey, value)
    get() = getBoolean(ClearLogsKey, false)

var PropertiesComponent.maxSnapshots: PositiveNumber
    set(value) = setValue(MaxSnapshotsKey, value.toInt(), DefaultMaxSnapshots.toInt())
    get() = PositiveNumber.of(getInt(MaxSnapshotsKey, DefaultMaxSnapshots.toInt()))

val Project.javaPsiFacade: JavaPsiFacade
    get() = JavaPsiFacade.getInstance(this)

private var PropertiesComponent.host: String?
    set(value) = setValue(HostKey, value)
    get() = getValue(HostKey)

private var PropertiesComponent.port: String?
    set(value) = setValue(PortKey, value)
    get() = getValue(PortKey)
