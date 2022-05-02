package io.github.xlopec.tea.time.travel.plugin.feature.component.model

import io.github.xlopec.tea.time.travel.plugin.util.map
import io.github.xlopec.tea.time.travel.plugin.model.ComponentMapping
import io.github.xlopec.tea.time.travel.plugin.model.FilteredSnapshot
import io.github.xlopec.tea.time.travel.plugin.model.Invalid
import io.github.xlopec.tea.time.travel.plugin.model.OriginalSnapshot
import io.github.xlopec.tea.time.travel.plugin.model.Valid
import io.github.xlopec.tea.time.travel.plugin.model.Value
import io.github.xlopec.tea.time.travel.plugin.model.filteredBy
import io.github.xlopec.tea.time.travel.plugin.model.toFiltered
import io.github.xlopec.tea.time.travel.protocol.ComponentId
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf

data class ComponentState(
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

fun ComponentState.updateFilter(
    filterInput: String,
    ignoreCase: Boolean,
    option: FilterOption
): ComponentState = updateFilter(Filter.new(filterInput, option, ignoreCase))

fun ComponentState.updateFilter(
    filter: Filter
): ComponentState {
    val filtered = when (val validatedPredicate = filter.predicate) {
        is Valid -> snapshots.filteredBy(validatedPredicate.t)
        is Invalid -> filteredSnapshots
    }

    return copy(filter = filter, filteredSnapshots = filtered)
}

data class DebugState(
    val components: ComponentMapping = persistentMapOf()
)
