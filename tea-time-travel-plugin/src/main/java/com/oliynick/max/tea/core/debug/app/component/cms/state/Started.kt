package com.oliynick.max.tea.core.debug.app.component.cms.state

import com.oliynick.max.tea.core.debug.app.domain.*
import com.oliynick.max.tea.core.debug.app.misc.mapNotNull
import com.oliynick.max.tea.core.debug.app.transport.Server
import com.oliynick.max.tea.core.debug.protocol.ComponentId
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

data class Started(
    override val settings: Settings,
    val debugState: DebugState,
    val server: Server
) : State

fun Started.update(
    debugState: DebugState
) = copy(debugState = debugState)

fun Started.removeSnapshots(
    id: ComponentId,
    snapshots: Set<SnapshotId>
) = updateComponents { mapping -> mapping.put(id, debugState.component(id).removeSnapshots(snapshots)) }

fun Started.removeSnapshots(
    id: ComponentId
) = updateComponents { mapping -> mapping.put(id, debugState.component(id).removeSnapshots()) }

inline fun Started.updateComponents(
    how: (mapping: ComponentMapping) -> ComponentMapping
) =
    update(debugState.copy(components = how(debugState.components)))

fun Started.updateComponent(
    id: ComponentId,
    how: (mapping: ComponentDebugState) -> ComponentDebugState?
) =
    update(debugState.updateComponent(id, how))

fun Started.snapshot(
    componentId: ComponentId,
    snapshotId: SnapshotId
): OriginalSnapshot = debugState.components[componentId]?.snapshots?.first { s -> s.meta.id == snapshotId }
    ?: error("Couldn't find a snapshot $snapshotId for component $componentId, available components ${debugState.components}")

fun ComponentDebugState.appendSnapshot(
    snapshot: OriginalSnapshot,
    state: Value
): ComponentDebugState {

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

fun ComponentDebugState.removeSnapshots(
    ids: Set<SnapshotId>
): ComponentDebugState =
    copy(
        snapshots = snapshots.removeAll { s -> s.meta.id in ids },
        filteredSnapshots = filteredSnapshots.removeAll { s -> s.meta.id in ids }
    )

fun ComponentDebugState.removeSnapshots(): ComponentDebugState =
    copy(
        snapshots = persistentListOf(),
        filteredSnapshots = persistentListOf()
    )

inline val DebugState.componentIds: ImmutableSet<ComponentId>
    get() = components.keys

fun DebugState.componentOrNull(
    id: ComponentId
) = components[id]

fun DebugState.component(
    id: ComponentId
) = components[id] ?: notifyUnknownComponent(id)

fun Started.state(
    componentId: ComponentId,
    snapshotId: SnapshotId
) = snapshot(componentId, snapshotId).state

fun Started.message(
    componentId: ComponentId,
    snapshotId: SnapshotId
) = snapshot(componentId, snapshotId).message

fun Started.updateFilter(
    id: ComponentId,
    filterInput: String,
    ignoreCase: Boolean,
    option: FilterOption
) = updateComponent(id) { s ->

    val filter = Filter.new(filterInput, option, ignoreCase)
    val filtered = when (val validatedPredicate = filter.predicate) {
        is Valid -> s.snapshots.filteredBy(validatedPredicate.t)
        is Invalid -> s.filteredSnapshots
    }

    s.copy(filter = filter, filteredSnapshots = filtered)
}

fun OriginalSnapshot.toFiltered() =
    FilteredSnapshot(
        meta,
        message,
        state
    )

fun OriginalSnapshot.filteredBy(
    predicate: Predicate
): FilteredSnapshot? {

    val m = message?.let { predicate.applyTo(it) }
    val s = predicate.applyTo(state)
    val c = predicate.applyToWrapper(commands)

    return if (m != null || s != null || c != null) {
        FilteredSnapshot(meta, m, s, c)
    } else {
        null
    }
}

private fun notifyUnknownComponent(
    id: ComponentId
): Nothing =
    throw IllegalArgumentException("Unknown component $id")

inline fun DebugState.updateComponent(
    id: ComponentId,
    crossinline how: (mapping: ComponentDebugState) -> ComponentDebugState?
) =
    copy(components = components.builder().also { m -> m.computeIfPresent(id) { _, s -> how(s) } }.build())

fun PersistentList<OriginalSnapshot>.filteredBy(
    predicate: Predicate
): PersistentList<FilteredSnapshot> =
    mapNotNull { o -> o.filteredBy(predicate) }
