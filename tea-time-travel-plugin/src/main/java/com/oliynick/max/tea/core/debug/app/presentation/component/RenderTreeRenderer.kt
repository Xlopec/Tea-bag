@file:Suppress("FunctionName")

package com.oliynick.max.tea.core.debug.app.presentation.component

import com.oliynick.max.tea.core.debug.app.presentation.ui.misc.*
import java.awt.Component
import javax.swing.JLabel
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreeCellRenderer
import javax.swing.tree.TreeModel

class RenderTreeRenderer private constructor(
    var formatter: ValueFormatter,
    private val transformer: RenderTree.(TreeModel, ValueFormatter) -> String
) : TreeCellRenderer {

    companion object {

        fun SnapshotsRenderer(
            formatter: ValueFormatter
        ) = RenderTreeRenderer(formatter, ::toReadableSnapshotString)

        fun StateRenderer(
            formatter: ValueFormatter
        ) = RenderTreeRenderer(formatter) { _, f -> toReadableStateString(this, f) }
    }

    override fun getTreeCellRendererComponent(
        tree: JTree,
        value: Any,
        selected: Boolean,
        expanded: Boolean,
        leaf: Boolean,
        row: Int,
        hasFocus: Boolean
    ): Component =
        JLabel().apply {

            val payload = (value as DefaultMutableTreeNode).userObject as RenderTree

            text = transformer(payload, tree.model, formatter)
            icon = payload.icon
        }

}

private fun toReadableStateString(
    renderTree: RenderTree,
    formatter: ValueFormatter
): String =
    when (renderTree) {
        RootNode -> "State"
        is SnapshotNode, is MessageNode, is StateNode -> error("Can't render $renderTree")
        is PropertyNode -> renderTree.toReadableString(formatter)
        is ValueNode -> renderTree.toReadableString(formatter)
        is IndexedNode -> renderTree.toReadableString(formatter)
        is EntryKeyNode -> renderTree.toReadableString(formatter)
        is EntryValueNode -> renderTree.toReadableString(formatter)
    }

private fun toReadableSnapshotString(
    renderTree: RenderTree,
    model: TreeModel,
    formatter: ValueFormatter
): String =
    when (renderTree) {
        RootNode -> "Snapshots (${model.getChildCount(model.root)})"
        is SnapshotNode -> renderTree.snapshot.toReadableString()
        is MessageNode -> "Message"
        is StateNode -> "State"
        is PropertyNode -> renderTree.toReadableString(formatter)
        is ValueNode -> renderTree.toReadableString(formatter)
        is IndexedNode -> renderTree.toReadableString(formatter)
        is EntryKeyNode -> renderTree.toReadableString(formatter)
        is EntryValueNode -> renderTree.toReadableString(formatter)
    }
