/*
 * Copyright (C) 2019 Maksym Oliinyk.
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

package com.oliynick.max.tea.core.debug.app.storage

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.project.Project
import com.oliynick.max.tea.core.debug.app.domain.DEFAULT_HOST
import com.oliynick.max.tea.core.debug.app.domain.DEFAULT_PORT
import com.oliynick.max.tea.core.debug.app.domain.ServerSettings
import com.oliynick.max.tea.core.debug.app.domain.Settings

const val PLUGIN_ID = "com.oliynick.max.tea.core.plugin"

val Project.properties: PropertiesComponent
    get() = PropertiesComponent.getInstance(this)

var PropertiesComponent.pluginSettings: Settings
    set(value) {
        serverSettings = value.serverSettings
        isDetailedToStringEnabled = value.isDetailedOutput
    }
    get() = Settings(serverSettings, isDetailedToStringEnabled)

var PropertiesComponent.isDetailedToStringEnabled: Boolean
    set(value) = setValue("$PLUGIN_ID.isDetailedToStringEnabled", value)
    get() = getBoolean("$PLUGIN_ID.isDetailedToStringEnabled", false)

var PropertiesComponent.serverSettings: ServerSettings
    set(value) {
        setValue("$PLUGIN_ID.host", value.host)
        setValue("$PLUGIN_ID.port", value.port.toInt(), 8080)
    }
    get() = ServerSettings(
        getValue("$PLUGIN_ID.host", DEFAULT_HOST),
        getInt("$PLUGIN_ID.port", DEFAULT_PORT.toInt()).toUInt()
    )
