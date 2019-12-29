package com.oliynick.max.elm.time.travel.app.domain.cms

import java.util.*

fun Started.update(debugState: DebugState) =
    copy(debugState = debugState)

fun Stopped.update(serverSettings: ServerSettings) =
    copy(settings = settings.copy(serverSettings = serverSettings))

inline fun Stopped.updatedServerSettings(how: ServerSettings.() -> ServerSettings): ServerSettings =
    settings.serverSettings.run(how)

inline fun Started.updateComponents(how: (mapping: ComponentMapping) -> ComponentMapping) =
    update(debugState.copy(components = how(debugState.components)))

fun ComponentDebugState.appendSnapshot(snapshot: Snapshot): ComponentDebugState {
    return copy(snapshots = snapshots + snapshot, currentState = snapshot.state)
}

fun ComponentDebugState.removeSnapshots(ids: Set<UUID>): ComponentDebugState {
    return copy(snapshots = snapshots.filter { snapshot -> !ids.contains(snapshot.id) })
}

fun ComponentDebugState.asPair() = id to this