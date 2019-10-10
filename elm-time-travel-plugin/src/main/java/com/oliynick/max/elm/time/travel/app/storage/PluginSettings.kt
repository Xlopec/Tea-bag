package com.oliynick.max.elm.time.travel.app.storage

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.project.Project
import com.oliynick.max.elm.time.travel.app.domain.ServerSettings
import com.oliynick.max.elm.time.travel.app.domain.Settings
import java.io.File

private const val PLUGIN_ID = "com.oliynick.max.elm.time.travel.plugin"

val Project.properties: PropertiesComponent
    get() = PropertiesComponent.getInstance(this)

var PropertiesComponent.pluginSettings: Settings
    set(value) {
        paths = value.classFiles
        serverSettings = value.serverSettings
    }
    get() = Settings(serverSettings, paths)

var PropertiesComponent.paths: List<File>
    set(value) = setValues("$PLUGIN_ID.paths", Array(value.size) { i -> value[i].absolutePath })
    get() = getValues("$PLUGIN_ID.paths")?.map(::File) ?: emptyList()

var PropertiesComponent.serverSettings: ServerSettings
    set(value) {
        setValue("$PLUGIN_ID.host", value.host)
        setValue("$PLUGIN_ID.port", value.port.toInt(), 8080)
    }
    get() = ServerSettings(getValue("$PLUGIN_ID.host", "0.0.0.0"), getInt("$PLUGIN_ID.port", 8080).toUInt())