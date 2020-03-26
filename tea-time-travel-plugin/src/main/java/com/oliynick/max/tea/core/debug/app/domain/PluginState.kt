package com.oliynick.max.tea.core.debug.app.domain

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import protocol.ComponentId
import java.time.LocalDateTime
import java.util.*

typealias ComponentMapping = PersistentMap<ComponentId, ComponentDebugState>

data class Snapshot(
    val id: UUID,
    val timestamp: LocalDateTime,
    val message: Value?,
    val state: Value?
)

data class ComponentDebugState(
    val id: ComponentId,
    val state: Value,
    val filter: Filter = Filter.empty(),
    val snapshots: PersistentList<Snapshot> = persistentListOf(),
    val filteredSnapshots: PersistentList<Snapshot> = snapshots
)

data class DebugState(
    val components: ComponentMapping = persistentMapOf()
)