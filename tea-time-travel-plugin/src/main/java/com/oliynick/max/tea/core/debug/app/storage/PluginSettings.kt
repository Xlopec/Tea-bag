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
import com.oliynick.max.tea.core.debug.app.domain.cms.ServerSettings
import com.oliynick.max.tea.core.debug.app.domain.cms.Settings
import com.oliynick.max.tea.core.debug.app.domain.cms.defaultHost
import com.oliynick.max.tea.core.debug.app.domain.cms.defaultPort
import java.io.File

private const val PLUGIN_ID = "com.oliynick.max.elm.time.travel.plugin"

val Project.properties: PropertiesComponent
    get() = PropertiesComponent.getInstance(this)

var PropertiesComponent.pluginSettings: Settings
    set(value) {
        serverSettings = value.serverSettings
    }
    get() = Settings(serverSettings)

@Deprecated("will be removed or replaced")
var PropertiesComponent.paths: List<File>
    set(value) = setValues("$PLUGIN_ID.paths", Array(value.size) { i -> value[i].absolutePath })
    get() = getValues("$PLUGIN_ID.paths")?.map(::File) ?: emptyList()

var PropertiesComponent.serverSettings: ServerSettings
    set(value) {
        setValue("$PLUGIN_ID.host", value.host)
        setValue("$PLUGIN_ID.port", value.port.toInt(), 8080)
    }
    get() = ServerSettings(
        getValue("$PLUGIN_ID.host", defaultHost),
        getInt("$PLUGIN_ID.port", defaultPort.toInt()).toUInt()
    )
