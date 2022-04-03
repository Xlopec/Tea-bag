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

import com.oliynick.max.tea.core.debug.app.misc.map
import com.oliynick.max.tea.core.debug.app.state.filteredBy
import com.oliynick.max.tea.core.debug.app.state.toFiltered
import com.oliynick.max.tea.core.debug.protocol.ComponentId
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import java.time.LocalDateTime
import java.util.*

typealias ComponentMapping = PersistentMap<ComponentId, ComponentDebugState>

@JvmInline
value class SnapshotId(
    val value: UUID
)

data class SnapshotMeta(
    val id: SnapshotId,
    val timestamp: LocalDateTime
)

data class OriginalSnapshot(
    val meta: SnapshotMeta,
    val message: Value?,
    val state: Value,
    val commands: CollectionWrapper,
)

data class FilteredSnapshot(
    val meta: SnapshotMeta,
    val message: Value? = null,
    val state: Value? = null,
    val commands: CollectionWrapper? = null,
) {
    init {
        require(message != null || state != null || commands != null) { "failed requirement $this" }
    }
}

data class ComponentDebugState(
    val id: ComponentId,
    val state: Value,
    // fixme we should re-model this bit, invariant checks should reside inside a dedicated entity
    val filter: Filter = Filter.empty(),
    val snapshots: PersistentList<OriginalSnapshot> = persistentListOf(),
    val filteredSnapshots: PersistentList<FilteredSnapshot> = persistentListOf()
) {
    /**
     * Creates already reset debug state
     */
    constructor(
        id: ComponentId,
        state: Value,
        snapshots: PersistentList<OriginalSnapshot>,
    ) : this(id, state, Filter.empty(), snapshots, snapshots.map(OriginalSnapshot::toFiltered))

    init {
        require(filteredSnapshots.size <= snapshots.size) {
            """
            filteredSnapshots.size > snapshots.size,
            snapshots=$snapshots",
            filtered=$filteredSnapshots
            """.trimIndent()
        }
    }

}

fun ComponentDebugState.updateFilter(
    filterInput: String,
    ignoreCase: Boolean,
    option: FilterOption
): ComponentDebugState = updateFilter(Filter.new(filterInput, option, ignoreCase))

fun ComponentDebugState.updateFilter(
    filter: Filter
): ComponentDebugState {
    val filtered = when (val validatedPredicate = filter.predicate) {
        is Valid -> snapshots.filteredBy(validatedPredicate.t)
        is Invalid -> filteredSnapshots
    }

    return copy(filter = filter, filteredSnapshots = filtered)
}

data class DebugState(
    val components: ComponentMapping = persistentMapOf()
)
