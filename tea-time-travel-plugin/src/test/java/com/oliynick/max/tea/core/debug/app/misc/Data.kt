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

@file:Suppress("TestFunctionName")

package com.oliynick.max.tea.core.debug.app.misc

import com.oliynick.max.tea.core.debug.app.component.cms.Started
import com.oliynick.max.tea.core.debug.app.domain.*
import com.oliynick.max.tea.core.debug.app.transport.Server
import com.oliynick.max.tea.core.debug.gson.GsonClientMessage
import com.oliynick.max.tea.core.debug.protocol.ComponentId
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentMap
import java.time.LocalDateTime
import java.util.*

val TestSettings = Settings(Valid(TestHost.value, TestHost), Valid(TestPort.value.toString(), TestPort), false)

val StartedTestServerStub = object : Server {
    override suspend fun stop() = Unit
    override suspend fun invoke(component: ComponentId, message: GsonClientMessage) = Unit
}

val TestTimestamp: LocalDateTime = LocalDateTime.of(2000, 1, 1, 1, 1)

val TestSnapshotId: SnapshotId  = SnapshotId(UUID.fromString("3853fab6-f20c-11ea-adc1-0242ac120002"))

inline fun ComponentDebugStates(
    range: CharRange = 'b'..'z',
    block: (strId: String) -> Pair<ComponentId, ComponentDebugState> = { strId ->
        NonEmptyComponentDebugState(ComponentId(strId), SnapshotMeta(RandomSnapshotId(), TestTimestamp))
    }
) = range
    .map { it.toString() }
    .map(block)

fun EmptyOriginalSnapshot(
    m: SnapshotMeta
) = OriginalSnapshot(m, Null, Null)

fun EmptyFilteredSnapshot(
    m: SnapshotMeta
) = FilteredSnapshot.ofBoth(m, Null, Null)

fun NonEmptyComponentDebugState(
    componentId: ComponentId,
    meta: SnapshotMeta
) = ComponentDebugState(
        componentId,
        persistentListOf(OriginalSnapshot(meta, Null, Null)),
        persistentListOf(FilteredSnapshot.ofBoth(meta, Null, Null))
)

fun ComponentDebugState(
    componentId: ComponentId,
    snapshots: PersistentList<OriginalSnapshot> = persistentListOf(),
    filteredSnapshots: PersistentList<FilteredSnapshot> = persistentListOf(),
    state: Value = Null
) = componentId to ComponentDebugState(componentId, state, snapshots = snapshots, filteredSnapshots = filteredSnapshots)

fun Started(
    states: Iterable<Pair<ComponentId, ComponentDebugState>>
) = Started(
        TestSettings,
        DebugState(states.toMap().toPersistentMap()),
        StartedTestServerStub
)

fun Started(
    vararg states: Pair<ComponentId, ComponentDebugState>
) = Started(
        TestSettings,
        DebugState(states.toMap().toPersistentMap()),
        StartedTestServerStub
)

fun RandomSnapshotId() = SnapshotId(UUID.randomUUID())
