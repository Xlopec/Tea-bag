@file:Suppress("FunctionName")

package com.oliynick.max.elm.time.travel.app.domain

import com.oliynick.max.elm.core.component.UpdateWith
import java.util.*

fun <Env> LiveUpdater() where Env : NotificationUpdater,
                              Env : UiUpdater =
    object : LiveUpdater<Env> {}

interface LiveUpdater<Env> : Updater<Env> where Env : NotificationUpdater,
                                                Env : UiUpdater {

    override fun Env.update(
        message: PluginMessage,
        state: PluginState
    ): UpdateWith<PluginState, PluginCommand> =
        when (message) {
            is UIMessage -> update(message, state)
            is NotificationMessage -> update(message, state)
        }
}

@Deprecated("will be removed")
inline fun <reified R : T, T> toExpected(
    t: T,
    crossinline message: () -> Any = { "Unexpected state, required ${R::class} but was $t" }
): R {
    require(t is R, message)
    return t
}

fun ComponentDebugState.appendSnapshot(snapshot: Snapshot): ComponentDebugState {
    return copy(snapshots = snapshots + snapshot, currentState = snapshot.state)
}

fun ComponentDebugState.removeSnapshots(ids: Set<UUID>): ComponentDebugState {
    return copy(snapshots = snapshots.filter { snapshot -> !ids.contains(snapshot.id) })
}

fun ComponentDebugState.asPair() = id to this