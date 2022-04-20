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

package io.github.xlopec.tea.core.debug.app.misc

import io.github.xlopec.tea.core.debug.protocol.ComponentId
import io.github.xlopec.tea.time.travel.gson.GsonClientMessage
import io.github.xlopec.tea.time.travel.plugin.domain.CollectionWrapper
import io.github.xlopec.tea.time.travel.plugin.domain.ComponentDebugState
import io.github.xlopec.tea.time.travel.plugin.domain.DebugState
import io.github.xlopec.tea.time.travel.plugin.domain.FilteredSnapshot
import io.github.xlopec.tea.time.travel.plugin.domain.Null
import io.github.xlopec.tea.time.travel.plugin.domain.OriginalSnapshot
import io.github.xlopec.tea.time.travel.plugin.domain.ServerAddress
import io.github.xlopec.tea.time.travel.plugin.domain.Settings
import io.github.xlopec.tea.time.travel.plugin.domain.SnapshotId
import io.github.xlopec.tea.time.travel.plugin.domain.SnapshotMeta
import io.github.xlopec.tea.time.travel.plugin.domain.Valid
import io.github.xlopec.tea.time.travel.plugin.domain.Value
import io.github.xlopec.tea.time.travel.plugin.state.Server
import io.github.xlopec.tea.time.travel.plugin.state.Started
import java.time.LocalDateTime
import java.util.UUID
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentMap

val TestSettings = Settings(Valid(TestHost.value, TestHost), Valid(TestPort.value.toString(), TestPort), false)

val StartedTestServerStub = object : Server {
    override val address: ServerAddress = ServerAddress(TestHost, TestPort)
    override suspend fun stop() = Unit
    override suspend fun invoke(component: ComponentId, message: GsonClientMessage) = Unit
}

val TestTimestamp1: LocalDateTime = LocalDateTime.of(2000, 1, 1, 1, 1)
val TestTimestamp2: LocalDateTime = LocalDateTime.of(2001, 2, 3, 4, 5)

val TestSnapshotId1: SnapshotId = SnapshotId(UUID.fromString("3853fab6-f20c-11ea-adc1-0242ac120002"))
val TestSnapshotId2: SnapshotId = SnapshotId(UUID.fromString("40811a0c-82ca-11ec-a8a3-0242ac120002"))

inline fun ComponentDebugStates(
    range: CharRange = 'b'..'z',
    block: (strId: String) -> Pair<ComponentId, ComponentDebugState> = { strId ->
        NonEmptyComponentDebugState(ComponentId(strId), SnapshotMeta(RandomSnapshotId(), TestTimestamp1))
    }
) = range
    .map { it.toString() }
    .map(block)

fun EmptyOriginalSnapshot(
    m: SnapshotMeta
) = OriginalSnapshot(m, Null, Null, CollectionWrapper(listOf()))

fun EmptyFilteredSnapshot(
    m: SnapshotMeta
) = FilteredSnapshot(m, Null, Null)

fun NonEmptyComponentDebugState(
    componentId: ComponentId,
    meta: SnapshotMeta
) = ComponentDebugState(
    componentId,
    persistentListOf(OriginalSnapshot(meta, Null, Null, CollectionWrapper(listOf()))),
    persistentListOf(FilteredSnapshot(meta, Null, Null))
)

fun ComponentDebugState(
    componentId: ComponentId,
    snapshots: PersistentList<OriginalSnapshot> = persistentListOf(),
    filteredSnapshots: PersistentList<FilteredSnapshot> = persistentListOf(),
    state: Value = Null
) = componentId to ComponentDebugState(componentId, state, snapshots = snapshots, filteredSnapshots = filteredSnapshots)

fun StartedFromPairs(
    states: Iterable<Pair<ComponentId, ComponentDebugState>>
): Started = Started(
    TestSettings,
    DebugState(states.toMap().toPersistentMap()),
    StartedTestServerStub
)

fun StartedFromPairs(
    vararg states: Pair<ComponentId, ComponentDebugState>
) = Started(
    TestSettings,
    DebugState(states.toMap().toPersistentMap()),
    StartedTestServerStub
)

fun RandomSnapshotId() = SnapshotId(UUID.randomUUID())
