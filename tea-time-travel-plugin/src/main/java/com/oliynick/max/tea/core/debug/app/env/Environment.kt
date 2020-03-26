package com.oliynick.max.tea.core.debug.app.env

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.project.Project
import com.oliynick.max.tea.core.debug.app.component.resolver.AppResolver
import com.oliynick.max.tea.core.debug.app.component.resolver.HasMessageChannel
import com.oliynick.max.tea.core.debug.app.component.resolver.HasMessagesChannel
import com.oliynick.max.tea.core.debug.app.component.resolver.HasProject
import com.oliynick.max.tea.core.debug.app.component.resolver.HasSystemProperties
import com.oliynick.max.tea.core.debug.app.component.resolver.LiveAppResolver
import com.oliynick.max.tea.core.debug.app.component.updater.LiveNotificationUpdater
import com.oliynick.max.tea.core.debug.app.component.updater.LiveUiUpdater
import com.oliynick.max.tea.core.debug.app.component.updater.LiveUpdater
import com.oliynick.max.tea.core.debug.app.component.updater.NotificationUpdater
import com.oliynick.max.tea.core.debug.app.component.updater.UiUpdater
import com.oliynick.max.tea.core.debug.app.component.updater.Updater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

interface Environment :
    Updater<Environment>,
    NotificationUpdater,
    UiUpdater,
    AppResolver<Environment>,
    HasMessageChannel,
    HasSystemProperties,
    HasProject,
    CoroutineScope

@Suppress("FunctionName")
fun Environment(
    properties: PropertiesComponent,
    project: Project
): Environment =
    object : Environment,
        Updater<Environment> by LiveUpdater(),
        NotificationUpdater by LiveNotificationUpdater,
        UiUpdater by LiveUiUpdater,
        AppResolver<Environment> by LiveAppResolver(),
        HasMessageChannel by HasMessagesChannel(),
        HasSystemProperties by HasSystemProperties(properties),
        HasProject by HasProject(project),
        CoroutineScope by CoroutineScope(SupervisorJob() + Dispatchers.Main) {}
