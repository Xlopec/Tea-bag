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

package com.oliynick.max.tea.core.debug.app.env

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.project.Project
import com.oliynick.max.tea.core.debug.app.component.resolver.*
import com.oliynick.max.tea.core.debug.app.component.updater.*
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
        HasServer,
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
            HasServer by HasServer(),
            CoroutineScope by CoroutineScope(SupervisorJob() + Dispatchers.Main) {}
