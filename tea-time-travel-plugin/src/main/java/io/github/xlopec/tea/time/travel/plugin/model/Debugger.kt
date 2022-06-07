package io.github.xlopec.tea.time.travel.plugin.model

import androidx.compose.runtime.Immutable
import io.github.xlopec.tea.time.travel.protocol.ComponentId
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf

typealias ComponentMapping = PersistentMap<ComponentId, DebuggableComponent>

@Immutable
data class Debugger(
    val components: ComponentMapping = persistentMapOf()
)

inline val Debugger.componentIds: ImmutableSet<ComponentId>
    get() = components.keys

fun Debugger.component(
    id: ComponentId
) = components[id] ?: throw IllegalArgumentException("Unknown component $id, debugger ${components.keys}")

inline fun Debugger.updateComponent(
    id: ComponentId,
    crossinline how: (mapping: DebuggableComponent) -> DebuggableComponent?
) =
    copy(components = components.builder().also { m -> m.computeIfPresent(id) { _, s -> how(s) } }.build())
