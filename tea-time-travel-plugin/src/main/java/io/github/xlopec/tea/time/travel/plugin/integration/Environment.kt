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

package io.github.xlopec.tea.time.travel.plugin.integration

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.project.Project
import io.github.xlopec.tea.time.travel.plugin.feature.notification.NotificationResolver
import io.github.xlopec.tea.time.travel.plugin.feature.server.ServerCommandResolver
import io.github.xlopec.tea.time.travel.plugin.feature.storage.StorageResolver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import java.util.concurrent.Executors

interface Environment :
    AppUpdater,
    AppResolver<Environment>,
    ServerCommandResolver,
    StorageResolver,
    NotificationResolver,
    CoroutineScope

fun Environment(
    properties: PropertiesComponent,
    project: Project,
    events: MutableSharedFlow<Message>,
): Environment = object : Environment,
    AppUpdater by AppUpdater(),
    StorageResolver by StorageResolver(properties),
    ServerCommandResolver by ServerCommandResolver(events),
    NotificationResolver by NotificationResolver(project),
    AppResolver<Environment> by AppResolver(),
    CoroutineScope by CoroutineScope(SupervisorJob() + Executors.newSingleThreadExecutor().asCoroutineDispatcher()) {}
