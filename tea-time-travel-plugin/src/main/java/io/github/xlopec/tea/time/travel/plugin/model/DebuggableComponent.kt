package io.github.xlopec.tea.time.travel.plugin.model

import io.github.xlopec.tea.time.travel.plugin.feature.component.model.Filter
import io.github.xlopec.tea.time.travel.plugin.feature.component.model.FilterOption
import io.github.xlopec.tea.time.travel.plugin.util.map
import io.github.xlopec.tea.time.travel.protocol.ComponentId
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

data class DebuggableComponent(
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

fun DebuggableComponent.updateFilter(
    filterInput: String,
    ignoreCase: Boolean,
    option: FilterOption
): DebuggableComponent = updateFilter(Filter.new(filterInput, option, ignoreCase))

fun DebuggableComponent.updateFilter(
    filter: Filter
): DebuggableComponent {
    val filtered = when (val validatedPredicate = filter.predicate) {
        is Valid -> snapshots.filteredBy(validatedPredicate.t)
        is Invalid -> filteredSnapshots
    }

    return copy(filter = filter, filteredSnapshots = filtered)
}

fun DebuggableComponent.appendSnapshot(
    snapshot: OriginalSnapshot,
    state: Value
): DebuggableComponent {

    val filtered = when (val validatedPredicate = filter.predicate) {
        is Valid -> snapshot.filteredBy(validatedPredicate.t)
        is Invalid -> snapshot.toFiltered()
    }

    return copy(
        snapshots = snapshots.add(snapshot),
        filteredSnapshots = filtered?.let(filteredSnapshots::add) ?: filteredSnapshots,
        state = state
    )
}

fun DebuggableComponent.removeSnapshots(
    ids: Set<SnapshotId>
): DebuggableComponent =
    copy(
        snapshots = snapshots.removeAll { s -> s.meta.id in ids },
        filteredSnapshots = filteredSnapshots.removeAll { s -> s.meta.id in ids }
    )

fun DebuggableComponent.removeSnapshots(): DebuggableComponent =
    copy(
        snapshots = persistentListOf(),
        filteredSnapshots = persistentListOf()
    )