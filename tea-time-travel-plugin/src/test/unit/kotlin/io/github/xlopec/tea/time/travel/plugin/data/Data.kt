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

package io.github.xlopec.tea.time.travel.plugin.data

import arrow.core.Invalid
import arrow.core.Valid
import com.google.gson.internal.LazilyParsedNumber
import io.github.xlopec.tea.time.travel.gson.GsonClientMessage
import io.github.xlopec.tea.time.travel.plugin.feature.settings.Host
import io.github.xlopec.tea.time.travel.plugin.feature.settings.Port
import io.github.xlopec.tea.time.travel.plugin.feature.settings.ServerAddress
import io.github.xlopec.tea.time.travel.plugin.feature.settings.Settings
import io.github.xlopec.tea.time.travel.plugin.model.*
import io.github.xlopec.tea.time.travel.protocol.ComponentId
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentMap
import java.time.LocalDateTime
import java.util.*

val TestHost = Host.newOrNull("localhost")!!
val TestPort = Port(123)

val ValidTestSettings = Settings(
    host = Input(TestHost.value, Valid(TestHost)), port = Input(TestPort.value.toString(), Valid(TestPort)),
    isDetailedOutput = false,
    clearSnapshotsOnAttach = true
)

val InvalidTestSettings = Settings(
    host = Input("abc", Invalid("Provide host")),
    port = Input("port", Invalid("Provide port")),
    isDetailedOutput = false,
    clearSnapshotsOnAttach = true
)

val StartedTestServerStub = object : Server {
    override val address: ServerAddress = ServerAddress(TestHost, TestPort)
    override suspend fun stop() = Unit
    override suspend fun invoke(component: ComponentId, message: GsonClientMessage) = Unit
}
val TestTimestamp1: LocalDateTime = LocalDateTime.of(2000, 1, 1, 1, 1)

val TestTimestamp2: LocalDateTime = LocalDateTime.of(2001, 2, 3, 4, 5)
val TestSnapshotId1: SnapshotId = SnapshotId(UUID.fromString("3853fab6-f20c-11ea-adc1-0242ac120001"))

val TestSnapshotId2: SnapshotId = SnapshotId(UUID.fromString("40811a0c-82ca-11ec-a8a3-0242ac120002"))

val TestSnapshotMeta1 = SnapshotMeta(TestSnapshotId1, TestTimestamp1)
val TestComponentId1 = ComponentId("Test component id")

val TestComponentId2 = ComponentId("Another Test component id")

inline fun ComponentDebugStates(
    range: CharRange = 'b'..'z',
    block: (strId: String) -> Pair<ComponentId, DebuggableComponent> = { strId ->
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
) = componentId to DebuggableComponent(componentId, state, snapshots = snapshots, filteredSnapshots = filteredSnapshots)

fun StartedFromPairs(
    settings: Settings,
    states: Iterable<Pair<ComponentId, DebuggableComponent>>,
    activeComponent: ComponentId? = states.toList().firstOrNull()?.first,
) = State(
    debugger = Debugger(settings = settings, components = states.toMap().toPersistentMap(), activeComponent = activeComponent),
    server = StartedTestServerStub
)

fun StartedFromPairs(
    settings: Settings,
    vararg states: Pair<ComponentId, DebuggableComponent>
) = State(
    debugger = Debugger(settings = settings, components = states.toMap().toPersistentMap()),
    server = StartedTestServerStub
)

fun RandomSnapshotId() = SnapshotId(UUID.randomUUID())
val TestUserValue = Ref(
    Type.of("com.max.oliynick.Test"),
    setOf(
        Property("name", StringWrapper("Max")),
        Property("surname", StringWrapper("Oliynick")),
        Property(
            "contacts",
            Ref(
                Type.of("com.max.oliynick.Contact"),
                setOf(
                    Property(
                        "site",
                        Ref(
                            Type.of("java.util.URL"),
                            setOf(
                                Property("domain", StringWrapper("google")),
                                // LazilyParsedNumber is workaround since Number != LazilyParsedNumber,
                                // LazilyParsedNumber might be compared only with other lazily parsed numbers
                                Property("port", NumberWrapper(LazilyParsedNumber(8080.toString()))),
                                Property("protocol", StringWrapper("https"))
                            ),
                        )
                    ),
                )
            )
        ),
        Property("position", StringWrapper("Developer")),
    )
)
val TestAppStateValue =
    Ref(
        Type.of("app.State"),
        setOf(
            Property(
                "users",
                CollectionWrapper(
                    listOf(
                        TestUserValue,
                        TestUserValue,
                        TestUserValue,
                        TestUserValue,
                        TestUserValue,
                    )
                )
            )
        )
    )
