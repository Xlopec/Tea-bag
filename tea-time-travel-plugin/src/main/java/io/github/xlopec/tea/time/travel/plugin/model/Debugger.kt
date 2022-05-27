package io.github.xlopec.tea.time.travel.plugin.model

import io.github.xlopec.tea.time.travel.protocol.ComponentId
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf

typealias ComponentMapping = PersistentMap<ComponentId, DebuggableComponent>

data class Debugger(
    val components: ComponentMapping = persistentMapOf()
)

inline val Debugger.componentIds: ImmutableSet<ComponentId>
    get() = components.keys

fun Debugger.component(
    id: ComponentId
) = components[id] ?: notifyUnknownComponent(id)

inline fun Debugger.updateComponent(
    id: ComponentId,
    crossinline how: (mapping: DebuggableComponent) -> DebuggableComponent?
) =
    copy(components = components.builder().also { m -> m.computeIfPresent(id) { _, s -> how(s) } }.build())

private fun notifyUnknownComponent(
    id: ComponentId
): Nothing =
    throw IllegalArgumentException("Unknown component $id")
