package com.oliynick.max.tea.core.debug.app.domain.cms

import kotlinx.collections.immutable.persistentListOf
import protocol.ComponentId
import java.util.*

fun Started.update(debugState: DebugState) =
    copy(debugState = debugState)

fun Started.removeSnapshots(
    id: ComponentId,
    snapshots: Set<UUID>
) = updateComponents { mapping -> mapping.put(id, debugState.component(id).removeSnapshots(snapshots)) }

fun Started.removeSnapshots(
    id: ComponentId
) = updateComponents { mapping -> mapping.put(id, debugState.component(id).removeSnapshots()) }

fun Stopped.update(serverSettings: ServerSettings) =
    copy(settings = settings.copy(serverSettings = serverSettings))

inline fun Stopped.updatedServerSettings(how: ServerSettings.() -> ServerSettings): ServerSettings =
    settings.serverSettings.run(how)

inline fun Started.updateComponents(how: (mapping: ComponentMapping) -> ComponentMapping) =
    update(debugState.copy(components = how(debugState.components)))

fun ComponentDebugState.appendSnapshot(snapshot: Snapshot): ComponentDebugState {
    return copy(snapshots = snapshots.add(snapshot), currentState = snapshot.state)
}

fun ComponentDebugState.removeSnapshots(ids: Set<UUID>): ComponentDebugState {
    return copy(snapshots = snapshots.removeAll { snapshot -> ids.contains(snapshot.id) })
}

fun ComponentDebugState.removeSnapshots(): ComponentDebugState {
    return copy(snapshots = persistentListOf())
}

fun DebugState.component(
    id: ComponentId
) = components[id] ?: notifyUnknownComponent(id)

fun notifyUnknownComponent(id: ComponentId): Nothing =
    throw IllegalArgumentException("Unknown component $id")
