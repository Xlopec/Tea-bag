package com.oliynick.max.elm.time.travel.app.misc

import com.oliynick.max.elm.time.travel.app.IntPrimitive
import com.oliynick.max.elm.time.travel.app.LevelNode
import com.oliynick.max.elm.time.travel.app.ObjectNode
import com.oliynick.max.elm.time.travel.app.StringPrimitive
import java.awt.Component
import javax.swing.JLabel
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreeCellRenderer

class ObjectTreeRenderer : JLabel(), TreeCellRenderer {

    override fun getTreeCellRendererComponent(
        tree: JTree,
        value: Any,
        selected: Boolean,
        expanded: Boolean,
        leaf: Boolean,
        row: Int,
        hasFocus: Boolean
    ): Component {

        val description = when (val obj = (value as DefaultMutableTreeNode).userObject as? ObjectNode ?: return this) {
            is LevelNode -> obj.type.typeName
            is IntPrimitive -> "Int: ${obj.value}"
            is StringPrimitive -> "String: ${obj.value}"
        }

        text = description

        return this
    }
}