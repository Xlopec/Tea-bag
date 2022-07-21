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

package io.github.xlopec.tea.time.travel.plugin.model

import androidx.compose.runtime.Stable
import io.github.xlopec.tea.time.travel.plugin.feature.settings.Settings
import io.github.xlopec.tea.time.travel.protocol.ComponentId

/**
 * This class represents plugin state. If server isn't null, then it's started,
 * it's considered to be in stopped state otherwise
 */
@Stable
data class State(
    val settings: Settings,
    val debugger: Debugger = Debugger(),
    val server: Server? = null,
)

val State.isStarted: Boolean
    get() = server != null

val State.isStopped: Boolean
    get() = !isStarted

val State.canStart: Boolean
    get() = settings.host.isValid() && settings.port.isValid() && isStopped

val State.canExport: Boolean
    get() = debugger.components.isNotEmpty()

val State.areSettingsModifiable: Boolean
    get() = !isStarted

val State.hasAttachedComponents: Boolean
    get() = debugger.components.isNotEmpty()

fun State.settings(
    settings: Settings,
) = copy(settings = settings)

fun State.snapshot(
    componentId: ComponentId,
    snapshotId: SnapshotId
): OriginalSnapshot = debugger.components[componentId]?.snapshots?.first { s -> s.meta.id == snapshotId }
    ?: error("Couldn't find a snapshot $snapshotId for component $componentId, available components ${debugger.components}")

fun State.state(
    componentId: ComponentId,
    snapshotId: SnapshotId
) = snapshot(componentId, snapshotId).state

fun State.debugger(
    debugger: Debugger
) = copy(debugger = debugger)
