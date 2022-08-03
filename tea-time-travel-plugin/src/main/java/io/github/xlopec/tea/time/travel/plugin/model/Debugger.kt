package io.github.xlopec.tea.time.travel.plugin.model

import androidx.compose.runtime.Immutable
import io.github.xlopec.tea.time.travel.plugin.feature.settings.Settings
import io.github.xlopec.tea.time.travel.plugin.feature.settings.ValidatedHost
import io.github.xlopec.tea.time.travel.plugin.feature.settings.ValidatedPort
import io.github.xlopec.tea.time.travel.protocol.ComponentId
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.persistentMapOf

// Ordering should be preserved, add tests for that
typealias ComponentMapping = PersistentMap<ComponentId, DebuggableComponent>

@Immutable
data class Debugger(
    val settings: Settings,
    val components: ComponentMapping = persistentMapOf(),
    val activeComponent: ComponentId? = components.values.firstOrNull()?.id
) {
    init {
        require(activeComponent == null || activeComponent in components) {
            "constraints violation, active=$activeComponent, keys=${components.keys.map(ComponentId::value)}}"
        }
        require(components.values.all { it.snapshots.size.toPositive() <= settings.maxSnapshots }) {
            "constraints violation, maxSnapshots=${settings.maxSnapshots.value}, components=${
                components.brokenComponents(settings.maxSnapshots).joinToString { "${it.id.value} -> ${it.snapshots.size}" }
            }"
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
) = putComponent(
    id,
    componentOrNew(id, newState).dropExtraSnapshots(settings.maxSnapshots).appendSnapshot(snapshot, newState)
)

fun Debugger.attachComponent(
    id: ComponentId,
    component: DebuggableComponent
) = putComponent(id, component.dropExtraSnapshots(settings.maxSnapshots)).selectComponent(id)

fun Debugger.attachComponent(
    id: ComponentId,
    state: Value,
    snapshot: OriginalSnapshot,
): Debugger {
    val componentState = if (settings.clearSnapshotsOnAttach) DebuggableComponent(id, state) else componentOrNew(id, state)
    // todo add option to disable component selection on attach
    return attachComponent(id, componentState.appendSnapshot(snapshot, state))
}

fun Debugger.updateFilter(
    id: ComponentId,
    filterInput: String,
    ignoreCase: Boolean,
    option: FilterOption
) = updateComponent(id, componentOrThrow(id).updateFilter(filterInput, ignoreCase, option))

fun Debugger.putComponent(
    id: ComponentId,
    component: DebuggableComponent,
) = copy(components = components.put(id, component))

fun Debugger.updateComponent(
    id: ComponentId,
    component: DebuggableComponent,
) = takeIf { id !in componentIds } ?: copy(components = components.put(id, component))

fun Debugger.removeComponent(
    id: ComponentId
) = copy(components = components.remove(id), activeComponent = nextSelectionForClosingTab(id))

fun Debugger.selectComponent(
    id: ComponentId
) = copy(activeComponent = id)

fun Debugger.settings(
    isDetailedOutput: Boolean,
    clearSnapshotsOnAttach: Boolean,
    maxSnapshots: PositiveNumber,
): Debugger = copy(
    settings = settings.copy(
        isDetailedOutput = isDetailedOutput,
        clearSnapshotsOnAttach = clearSnapshotsOnAttach,
        maxSnapshots = maxSnapshots
    ),
    components = if (maxSnapshots >= settings.maxSnapshots) components else components.dropExtraSnapshots(maxSnapshots)
)

internal fun Debugger.settings(
    hostInput: String?,
    portInput: String?,
) = copy(
    settings = settings.copy(
        host = ValidatedHost(hostInput),
        port = ValidatedPort(portInput)
    )
)

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

internal fun ComponentMapping.brokenComponents(
    maxSnapshots: PositiveNumber
) = values.filter { it.snapshots.size.toPositive() > maxSnapshots }

private fun Debugger.componentOrNew(
    id: ComponentId,
    state: Value,
) = components[id] ?: DebuggableComponent(id, state)

private fun ComponentMapping.dropExtraSnapshots(
    maxSnapshots: PositiveNumber
): ComponentMapping = mutate {
    // fixme throws ConcurrentModificationException, see https://github.com/Kotlin/kotlinx.collections.immutable/issues/68
    // it.replaceAll { _, u -> u.dropExtraSnapshots(maxSnapshots) }
    val keys = keys
    keys.forEach { key ->
        it[key] = it[key]!!.dropExtraSnapshots(maxSnapshots)
    }
}

private fun DebuggableComponent.dropExtraSnapshots(
    maxSnapshots: PositiveNumber
): DebuggableComponent {
    var filteredSnapshots = filteredSnapshots
    var snapshots = snapshots
    // [0..UInt.MAX] > [1..UInt.MAX]
    while (snapshots.size.toUInt() > maxSnapshots.toUInt()) {
        val temp = snapshots.first()
        snapshots = snapshots.removeAt(0)
        // Works only when filtered snapshots preserve original snapshots ordering
        if (temp.meta == filteredSnapshots.firstOrNull()?.meta) {
            filteredSnapshots = filteredSnapshots.removeAt(0)
        }
    }

    return takeIf { it.snapshots === snapshots && it.filteredSnapshots === filteredSnapshots }
        ?: copy(snapshots = snapshots, filteredSnapshots = filteredSnapshots)
}

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
