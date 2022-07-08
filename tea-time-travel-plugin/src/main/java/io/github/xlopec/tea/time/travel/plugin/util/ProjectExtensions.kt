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

const val PluginId = "io.github.xlopec.tea.core.plugin"

val Project.properties: PropertiesComponent
    get() = PropertiesComponent.getInstance(this)

var PropertiesComponent.settings: Settings
    set(value) {
        host = value.host.input
        port = value.port.input
        isDetailedToStringEnabled = value.isDetailedOutput
        clearSnapshotsOnComponentAttach = value.clearSnapshotsOnAttach
    }
    get() = Settings.of(host, port, isDetailedToStringEnabled, clearSnapshotsOnComponentAttach)

// todo reduce visibility
var PropertiesComponent.isDetailedToStringEnabled: Boolean
    set(value) = setValue("$PluginId.isDetailedToStringEnabled", value)
    get() = getBoolean("$PluginId.isDetailedToStringEnabled", false)

var PropertiesComponent.clearSnapshotsOnComponentAttach: Boolean
    set(value) = setValue("$PluginId.clearLogsOnComponentAttach", value)
    get() = getBoolean("$PluginId.clearLogsOnComponentAttach", false)

val Project.javaPsiFacade: JavaPsiFacade
    get() = JavaPsiFacade.getInstance(this)

private var PropertiesComponent.host: String?
    set(value) = setValue("$PluginId.host", value)
    get() = getValue("$PluginId.host")

private var PropertiesComponent.port: String?
    set(value) = setValue("$PluginId.port", value)
    get() = getValue("$PluginId.port")
