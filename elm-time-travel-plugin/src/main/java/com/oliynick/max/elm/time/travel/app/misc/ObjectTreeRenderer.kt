package com.oliynick.max.elm.time.travel.app.misc

import com.oliynick.max.elm.time.travel.app.*
import org.kodein.di.simpleErasedName
import java.awt.Component
import java.io.PrintWriter
import java.io.StringWriter
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

        if (value === tree.model.root) {
            text = "Commands"
            return this
        }

        val description: String =
            when (val obj = (value as DefaultMutableTreeNode).userObject as? Node ?: return this) {
                is LevelNode -> obj.toReadableString()
                is IntPrimitive -> obj.toReadableString()
                is StringPrimitive -> obj.toReadableString()
                is CollectionPrimitive -> obj.toReadableString()
                is Instance -> obj.toReadableString()
            }

        text = description

        return this
    }
}

private fun Node.toReadableString(): String = when (this) {
    is LevelNode -> toReadableString()
    is IntPrimitive -> toReadableString()
    is StringPrimitive -> toReadableString()
    is CollectionPrimitive -> toReadableString()
    is Instance -> toReadableString()
}

private fun LevelNode.toReadableString(): String {
    return "$fieldName=${type.simpleErasedName()}(${nodes.joinToString { it.toReadableString() }})"
}

private fun Instance.toReadableString(): String {
    return "${type.simpleErasedName()}(${nodes.joinToString { it.toReadableString() }})"
}

private fun CollectionPrimitive.toReadableString(): String {
    return "${value::class.java.simpleErasedName()} ${nodes.joinToString(prefix = "[", postfix = "]") { it.toReadableString() }}"
}

private fun Any?.toStringCatching(): String {
    return try {
        toString()
    } catch (th: Throwable) {
        val sw = StringWriter()
        val pw = PrintWriter(sw)

        th.printStackTrace(pw)
        "Couldn't evaluate 'toString()', the method has thrown exception:\n$sw"
    }
}

private fun IntPrimitive.toReadableString() = "Int: $value"

private fun StringPrimitive.toReadableString() = "String: \"$value\""