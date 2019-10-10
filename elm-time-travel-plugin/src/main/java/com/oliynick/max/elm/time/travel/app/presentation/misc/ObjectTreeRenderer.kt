package com.oliynick.max.elm.time.travel.app.presentation.misc

import com.oliynick.max.elm.time.travel.app.domain.*
import java.awt.Component
import javax.swing.JLabel
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreeCellRenderer

internal class ObjectTreeRenderer(private val rootNodeName: String) : JLabel(), TreeCellRenderer {

    override fun getTreeCellRendererComponent(
        tree: JTree,
        value: Any,
        selected: Boolean,
        expanded: Boolean,
        leaf: Boolean,
        row: Int,
        hasFocus: Boolean
    ): Component {

        if (value === tree.model.root) {
            text = rootNodeName
            return this
        }

        text = when (val node = value.node) {
            is AnyRef -> node.toReadableString()
            is IntPrimitive -> node.toReadableString()
            is StringPrimitive -> node.toReadableString()
            is IterablePrimitive -> node.toReadableString()
            is NullRef -> node.toReadableString()
            is MapPrimitive -> node.toReadableString()
            is ArrayPrimitive -> node.toReadableString()
        }

        return this
    }

    private val Any.node inline get() = (this as DefaultMutableTreeNode).userObject as TypeNode

}