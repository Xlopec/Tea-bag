package com.oliynick.max.tea.core.debug.app.presentation.settings

import com.intellij.util.messages.Topic

interface PluginSettingsNotifier {

    companion object {
        val TOPIC = Topic.create("Plugin Settings Notifier", PluginSettingsNotifier::class.java)
    }

    fun onSettingsUpdated(
        isDetailedToStringEnabled: Boolean
    )

}
