package io.github.xlopec.tea.time.travel.plugin.model

import androidx.compose.runtime.Immutable
import io.github.xlopec.tea.time.travel.plugin.util.map
import io.github.xlopec.tea.time.travel.protocol.ComponentId
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

/**
 * Represents state of a component under debug.
 *
 * Invariants:
 * * filteredSnapshots.size <= snapshots.size
 * * filteredSnapshots.ids in snapshots.ids
 */
@Immutable
data class DebuggableComponent(
    val id: ComponentId,
    val state: Value,
    val filter: Filter = Filter.empty(),
    val snapshots: PersistentList<OriginalSnapshot> = persistentListOf(),
    val filteredSnapshots: PersistentList<FilteredSnapshot> = persistentListOf()
) {
    /**
     * Creates a DebuggableComponent with reset state
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
    val filtered = filter.predicate.value.fold({ filteredSnapshots }, { snapshots.filteredBy(it) })
    return copy(filter = filter, filteredSnapshots = filtered)
}

fun DebuggableComponent.appendSnapshot(
    snapshot: OriginalSnapshot,
    state: Value
): DebuggableComponent {
    val filtered = filter.predicate.value.fold({ snapshot.toFiltered() }, { snapshot.filteredBy(it) })
    return copy(
        snapshots = snapshots.add(snapshot),
        filteredSnapshots = filtered?.let(filteredSnapshots::add) ?: filteredSnapshots,
        state = state
    )
}

fun DebuggableComponent.removeSnapshotAt(
    index: Int
): DebuggableComponent =
    copy(
        snapshots = snapshots.removeAt(index),
        filteredSnapshots = filteredSnapshots.removeAt(index),
    )

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
