package io.github.xlopec.tea.time.travel.plugin.model

import androidx.compose.runtime.Immutable
import io.github.xlopec.tea.time.travel.protocol.ComponentId
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf

// Ordering should be preserved, add tests for that
typealias ComponentMapping = PersistentMap<ComponentId, DebuggableComponent>

@Immutable
data class Debugger(
    val components: ComponentMapping = persistentMapOf(),
    val activeComponent: ComponentId? = components.values.firstOrNull()?.id
) {
    init {
        require(activeComponent == null || activeComponent in components) {
            "constraints violation, active=$activeComponent, keys=${components.keys.map(ComponentId::value)}}"
        }
    }
}

inline val Debugger.componentIds: ImmutableSet<ComponentId>
    get() = components.keys

operator fun Debugger.get(
    id: ComponentId
) = componentOrThrow(id)

fun Debugger.componentOrThrow(
    id: ComponentId
) = components[id] ?: throw IllegalArgumentException("Unknown component $id, debugger ${components.keys}")

fun Debugger.removeSnapshots(
    id: ComponentId,
    snapshots: Set<SnapshotId>
) = updateComponent(id, componentOrThrow(id).removeSnapshots(snapshots))

fun Debugger.removeSnapshots(
    id: ComponentId,
) = updateComponent(id, componentOrThrow(id).removeSnapshots())

fun Debugger.appendSnapshot(
    id: ComponentId,
    snapshot: OriginalSnapshot,
    newState: Value,
    maxRetainedSnapshots: PositiveNumber,
): Debugger {
    val component = componentOrNew(id, newState)
    val normalizedComponent = if (component.exceedsMaxRetainedSnapshots(maxRetainedSnapshots)) {
        component.removeSnapshotAt(0)
    } else {
        component
    }

    val updated = normalizedComponent.appendSnapshot(snapshot, newState)

    return updateComponent(updated.id, updated)
}

fun Debugger.attachComponent(
    id: ComponentId,
    component: DebuggableComponent
) = updateComponent(id, component).selectComponent(id)

fun Debugger.attachComponent(
    id: ComponentId,
    state: Value,
    snapshot: OriginalSnapshot,
    clearSnapshotsOnAttach: Boolean
): Debugger {
    val componentState = if (clearSnapshotsOnAttach) DebuggableComponent(id, state) else componentOrNew(id, state)
    // todo add option to disable component selection on attach
    return attachComponent(id, componentState.appendSnapshot(snapshot, state))
}

fun Debugger.updateFilter(
    id: ComponentId,
    filterInput: String,
    ignoreCase: Boolean,
    option: FilterOption
) = updateComponent(id, componentOrThrow(id).updateFilter(filterInput, ignoreCase, option))

fun Debugger.updateComponent(
    id: ComponentId,
    component: DebuggableComponent,
) = copy(components = components.put(id, component))

fun Debugger.removeComponent(
    id: ComponentId
) = copy(components = components.remove(id), activeComponent = nextSelectionForClosingTab(id))

fun Debugger.selectComponent(
    id: ComponentId
) = copy(activeComponent = id)

/**
 * Returns next [component id][ComponentId] that should be selected among others after [closingTab] is removed
 * from current [debugger][Debugger] instance
 **/
internal fun Debugger.nextSelectionForClosingTab(
    closingTab: ComponentId,
): ComponentId? {
    require(components.isNotEmpty()) { "Can't calculate next selection for empty set of components $this" }
    // if activeComponent != closingTab, then we don't need to recalculate tab
    // if nextSelectionForClosingTab returns closingTab, then our components ComponentsMapping will be cleared, tab selection should be
    // reset as well
    return activeComponent.takeIf { it != closingTab } ?: components.nextSelectionForClosingTab(closingTab).takeIf { it != closingTab }
}

private fun Debugger.componentOrNew(
    id: ComponentId,
    state: Value,
) = components[id] ?: DebuggableComponent(id, state)

internal fun <K> PersistentMap<K, *>.nextSelectionForClosingTab(
    closingTabKey: K
): K {
    val currentSelectionIndex = keys.indexOf(closingTabKey)

    require(currentSelectionIndex >= 0) {
        "There is no component $closingTabKey inside debugger instance, available components: $keys"
    }

    return keys[calculateNextSelectionIndex(currentSelectionIndex, size)]
}

/**
 * Returns next selection index. Selection rules are the following:
 * * if current tab is in the middle of tab bar, then next left tab is chosen;
 * * if there are no tabs on the left side, then next right tab is chosen.
 * * else 0 is returned (the case when collection consists of a single element)
 *
 * E.g. after we close tab B (selected tab), given tabs a#B#c, we'll have tabs A#c (A tab is selected)
 */
private fun calculateNextSelectionIndex(
    currentSelectionIndex: Int,
    size: Int,
): Int {
    require(size > 0) { "Can't calculate selection index for empty collection" }
    require(currentSelectionIndex in 0 until size) {
        "Precondition 0 < $currentSelectionIndex (currentSelectionIndex) < $size (size) doesn't hold"
    }
    return (currentSelectionIndex - 1).takeIf { it >= 0 } ?: ((currentSelectionIndex + 1) % size)
}

private fun DebuggableComponent.exceedsMaxRetainedSnapshots(
    maxRetainedSnapshots: PositiveNumber,
) = snapshots.size >= maxRetainedSnapshots.value.toInt()

private operator fun <T> Collection<T>.get(
    i: Int
): T {
    if (this is List<T>) {
        return get(i)
    }

    forEachIndexed { index, t ->
        if (index == i) {
            return t
        }
    }
    error("There is no element for index $i, $this")
}
