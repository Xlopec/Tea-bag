@file:Suppress("FunctionName")

package com.oliynick.max.tea.core.debug.app.domain.resolver

import com.intellij.ide.util.PropertiesComponent

fun HasSystemProperties(properties: PropertiesComponent) =
    object : HasSystemProperties {
        override val properties: PropertiesComponent = properties
    }

interface HasSystemProperties {
    val properties: PropertiesComponent
}