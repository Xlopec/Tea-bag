package com.oliynick.max.elm.time.travel.app.presentation.misc

import com.oliynick.max.elm.time.travel.app.domain.cms.Snapshot
import com.oliynick.max.elm.time.travel.app.domain.cms.*

sealed class RenderTree
object RootNode : RenderTree()
data class SnapshotNode(val snapshot: Snapshot) : RenderTree()
data class MessageNode(val message: Value<*>) : RenderTree()
data class StateNode(val state: Value<*>) : RenderTree()
data class PropertyNode(val property: Property<*>) : RenderTree()
data class ValueNode(val value: Value<*>) : RenderTree()
data class IndexedNode(
    val index: Int,
    val value: Value<*>
) : RenderTree()

data class EntryKeyNode(val key: Value<*>) : RenderTree()
data class EntryValueNode(val value: Value<*>) : RenderTree()