package com.oliynick.max.tea.core.debug.app.component.cms

import com.oliynick.max.tea.core.debug.app.domain.ComponentDebugState
import com.oliynick.max.tea.core.debug.app.domain.ComponentMapping
import com.oliynick.max.tea.core.debug.app.domain.DebugState
import com.oliynick.max.tea.core.debug.app.domain.Filter
import com.oliynick.max.tea.core.debug.app.domain.FilterOption
import com.oliynick.max.tea.core.debug.app.domain.Invalid
import com.oliynick.max.tea.core.debug.app.domain.Predicate
import com.oliynick.max.tea.core.debug.app.domain.ServerSettings
import com.oliynick.max.tea.core.debug.app.domain.Snapshot
import com.oliynick.max.tea.core.debug.app.domain.Valid
import com.oliynick.max.tea.core.debug.app.domain.Value
import com.oliynick.max.tea.core.debug.app.domain.applyTo
import com.oliynick.max.tea.core.debug.app.domain.fold
import com.oliynick.max.tea.core.debug.app.misc.mapInPlace
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import protocol.ComponentId
import java.util.*

fun Started.update(
    debugState: DebugState
) = copy(debugState = debugState)

fun Started.removeSnapshots(
    id: ComponentId,
    snapshots: Set<UUID>
) = updateComponents { mapping -> mapping.put(id, debugState.component(id).removeSnapshots(snapshots)) }

fun Started.removeSnapshots(
    id: ComponentId
) = updateComponents { mapping -> mapping.put(id, debugState.component(id).removeSnapshots()) }

fun Stopped.update(
    serverSettings: ServerSettings
) = copy(settings = settings.copy(serverSettings = serverSettings))

inline fun Stopped.updatedServerSettings(
    how: ServerSettings.() -> ServerSettings
): ServerSettings =
    settings.serverSettings.run(how)

inline fun Started.updateComponents(
    how: (mapping: ComponentMapping) -> ComponentMapping
) =
    update(debugState.copy(components = how(debugState.components)))

fun Started.updateComponent(
    id: ComponentId,
    how: (mapping: ComponentDebugState) -> ComponentDebugState?
) =
    update(debugState.updateComponent(id, how))

inline fun DebugState.updateComponent(
    id: ComponentId,
    crossinline how: (mapping: ComponentDebugState) -> ComponentDebugState?
) =
    copy(components = components.builder().also { m -> m.computeIfPresent(id) { _, s -> how(s) } }.build())

fun ComponentDebugState.appendSnapshot(
    snapshot: Snapshot,
    state: Value
): ComponentDebugState =
    copy(
        snapshots = snapshots.add(snapshot),
        filteredSnapshots = filteredSnapshots.add(filter.predicate?.fold({ v -> snapshot.filteredBy(v.t) }, { snapshot }) ?: snapshot),
        state = state
    )

fun ComponentDebugState.removeSnapshots(
    ids: Set<UUID>
): ComponentDebugState {

    fun predicate(
        s: Snapshot
    ) = s.id in ids

    return copy(
        snapshots = snapshots.removeAll(::predicate),
        filteredSnapshots = filteredSnapshots.removeAll(::predicate)
    )
}

fun ComponentDebugState.removeSnapshots(): ComponentDebugState =
    copy(
        snapshots = persistentListOf(),
        filteredSnapshots = persistentListOf()
    )

fun DebugState.component(
    id: ComponentId
) = components[id] ?: notifyUnknownComponent(id)

fun Started.updateFilter(
    id: ComponentId,
    filterInput: String,
    ignoreCase: Boolean,
    option: FilterOption
) = updateComponent(id) { s ->

    val filter = Filter.new(filterInput, option, ignoreCase)
    val filtered = when(val predicate = filter.predicate) {
        is Valid -> s.snapshots.filteredBy(predicate.t)
        is Invalid -> s.filteredSnapshots
        null -> s.snapshots
    }

    s.copy(filter = filter, filteredSnapshots = filtered)
}

fun PersistentList<Snapshot>.filteredBy(
    predicate: Predicate
): PersistentList<Snapshot> =
    builder().mapInPlace { s -> s.filteredBy(predicate) }.build()

fun Snapshot.filteredBy(
    predicate: Predicate
) = copy(
    message = message?.let { applyTo(it, predicate) },
    state = state?.let { applyTo(it, predicate) }
)

fun notifyUnknownComponent(
    id: ComponentId
): Nothing =
    throw IllegalArgumentException("Unknown component $id")
