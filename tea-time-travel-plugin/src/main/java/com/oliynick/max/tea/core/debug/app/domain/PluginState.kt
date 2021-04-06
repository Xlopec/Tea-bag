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

package com.oliynick.max.tea.core.debug.app.domain

import com.oliynick.max.tea.core.debug.protocol.ComponentId
import kotlinx.collections.immutable.*
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
