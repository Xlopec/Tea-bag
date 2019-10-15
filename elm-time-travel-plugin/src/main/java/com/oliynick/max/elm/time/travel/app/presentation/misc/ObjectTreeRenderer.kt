package com.oliynick.max.elm.time.travel.app.presentation.misc

import com.oliynick.max.elm.time.travel.app.domain.*
import com.oliynick.max.elm.time.travel.app.presentation.sidebar.icon
import java.awt.Component
import javax.swing.Icon
import javax.swing.JLabel
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreeCellRenderer

internal class ObjectTreeRenderer(private val rootNodeName: String) : TreeCellRenderer {

    override fun getTreeCellRendererComponent(
        tree: JTree,
        value: Any,
        selected: Boolean,
        expanded: Boolean,
        leaf: Boolean,
        row: Int,
        hasFocus: Boolean
    ): Component {

        val label = JLabel()

        if (value === tree.model.root) {
            label.text = "$rootNodeName (${tree.model.getChildCount(tree.model.root)})"
            return label
        }

        val payload = (value as DefaultMutableTreeNode).userObject

        label.text = when(payload) {
            is Snapshot -> "${payload.timestamp}: ${payload.id}..."
            "Message", "State" -> payload.toString()
            is TypeNode -> payload.toReadableString()
            else -> throw IllegalArgumentException("Unknown value, was $payload")
        }

        label.icon = when(payload) {
            is Snapshot -> null
            "Message", "State" -> null
            is TypeNode -> payload.icon()
            else -> throw IllegalArgumentException("Unknown value, was $payload")
        }

        return label
    }

    private val Any.node inline get() = (this as DefaultMutableTreeNode).userObject as TypeNode

}

private fun TypeNode.icon(): Icon? {
    return when (this) {
        is ArrayPrimitive, is MapPrimitive, is IterablePrimitive, is AnyRef -> icon("class")
        NullRef -> null
        is IntPrimitive , is StringPrimitive -> icon("variable")
    }
}

/*
private fun TypeNode.toReadableString(): String {
    return when (this) {
        is AnyRef -> toReadableString()
        is IntPrimitive -> toReadableString()
        is StringPrimitive -> toReadableString()
        is IterablePrimitive -> toReadableString()
        is NullRef -> toReadableString()
        is MapPrimitive -> toReadableString()
        is ArrayPrimitive -> toReadableString()
    }
}*/
