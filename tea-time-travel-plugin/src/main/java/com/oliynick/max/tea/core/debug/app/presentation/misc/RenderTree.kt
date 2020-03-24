package com.oliynick.max.tea.core.debug.app.presentation.misc

import com.oliynick.max.tea.core.debug.app.domain.cms.Property
import com.oliynick.max.tea.core.debug.app.domain.cms.Snapshot
import com.oliynick.max.tea.core.debug.app.domain.cms.Value

sealed class RenderTree
object RootNode : RenderTree()
data class SnapshotNode(val snapshot: Snapshot) : RenderTree()
data class MessageNode(val message: Value) : RenderTree()
data class StateNode(val state: Value) : RenderTree()
data class PropertyNode(val property: Property) : RenderTree()
data class ValueNode(val value: Value) : RenderTree()
data class IndexedNode(
    val index: Int,
    val value: Value
) : RenderTree()

data class EntryKeyNode(val key: Value) : RenderTree()
data class EntryValueNode(val value: Value) : RenderTree()