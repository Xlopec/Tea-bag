package com.oliynick.max.tea.core.debug.app.domain

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import protocol.ComponentId
import java.time.LocalDateTime
import java.util.*

typealias ComponentMapping = PersistentMap<ComponentId, ComponentDebugState>

inline class SnapshotId(
    val value: UUID
)

data class SnapshotMeta(
    val id: SnapshotId,
    val timestamp: LocalDateTime
)

data class OriginalSnapshot(
    val meta: SnapshotMeta,
    val message: Value,
    val state: Value
)

class FilteredSnapshot private constructor(
    val meta: SnapshotMeta,
    val message: Value? = null,
    val state: Value? = null
) {

    companion object {

        fun ofMessage(
            meta: SnapshotMeta,
            message: Value
        ) = FilteredSnapshot(meta, message)

        fun ofState(
            meta: SnapshotMeta,
            state: Value
        ) = FilteredSnapshot(meta, state = state)

        fun ofBoth(
            meta: SnapshotMeta,
            message: Value,
            state: Value
        ) = FilteredSnapshot(meta, message, state)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FilteredSnapshot

        if (meta != other.meta) return false
        if (message != other.message) return false
        if (state != other.state) return false

        return true
    }

    override fun hashCode(): Int {
        var result = meta.hashCode()
        result = 31 * result + (message?.hashCode() ?: 0)
        result = 31 * result + (state?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String = "FilteredSnapshot(meta=$meta, message=$message, state=$state)"

}

data class ComponentDebugState(
    val id: ComponentId,
    val state: Value,
    val filter: Filter = Filter.empty(),
    val snapshots: PersistentList<OriginalSnapshot> = persistentListOf(),
    val filteredSnapshots: PersistentList<FilteredSnapshot> = persistentListOf()
)

data class DebugState(
    val components: ComponentMapping = persistentMapOf()
)