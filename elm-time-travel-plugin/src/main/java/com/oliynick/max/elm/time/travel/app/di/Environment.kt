package com.oliynick.max.elm.time.travel.app.di

import com.intellij.ide.util.PropertiesComponent
import com.oliynick.max.elm.time.travel.app.domain.resolver.*
import com.oliynick.max.elm.time.travel.app.domain.updater.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

interface Environment :
    Updater<Environment>,
    NotificationUpdater,
    UiUpdater,
    AppResolver<Environment>,
    HasChannels,
    HasServerService,
    HasSystemProperties,
    CoroutineScope

@Suppress("FunctionName")
fun Environment(properties: PropertiesComponent): Environment =
    object : Environment,
        Updater<Environment> by LiveUpdater(),
        NotificationUpdater by LiveNotificationUpdater,
        UiUpdater by LiveUiUpdater,
        AppResolver<Environment> by LiveAppResolver(),
        HasChannels by HasChannels(),
        HasServerService by HasServerService(),
        HasSystemProperties by HasSystemProperties(
            properties),
        CoroutineScope by CoroutineScope(SupervisorJob() + Dispatchers.Main) {}
