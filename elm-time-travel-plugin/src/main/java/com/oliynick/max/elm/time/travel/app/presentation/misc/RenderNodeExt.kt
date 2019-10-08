package com.oliynick.max.elm.time.travel.app.presentation.misc

import com.oliynick.max.elm.time.travel.app.domain.*
import org.kodein.di.simpleErasedName
import javax.swing.tree.DefaultMutableTreeNode

fun TypeNode.toJTree(): DefaultMutableTreeNode {

    fun fillJTree(node: TypeNode, parent: DefaultMutableTreeNode) {
        val current = DefaultMutableTreeNode(node)
            .also(parent::add)

        when(node) {
            is AnyRef -> node.fields.forEach { fillJTree(it.value, current) }
            is IterablePrimitive -> node.fields.forEach { fillJTree(it.value, current) }
            is MapPrimitive -> node.fields.forEach { fillJTree(it.value, current) }
            is ArrayPrimitive -> node.fields.forEach { fillJTree(it.value, current) }
            is NullRef, is IntPrimitive, is StringPrimitive -> Unit
        }.safe
    }

    return DefaultMutableTreeNode(this, true)
        .also { root -> fillJTree(this@toJTree, root) }
}

fun TypeNode.toReadableString(): String = when (this) {
    is AnyRef -> toReadableString()
    is IntPrimitive -> toReadableString()
    is StringPrimitive -> toReadableString()
    is IterablePrimitive -> toReadableString()
    is NullRef -> toReadableString()
    is MapPrimitive -> toReadableString()
    is ArrayPrimitive -> toReadableString()
}

fun AnyRef.toReadableString(): String {
    return "${type.simpleErasedName()}(${fields.joinToString(transform = ::toReadableString)})"
}

fun IterablePrimitive.toReadableString(): String {
    return "${value::class.java.simpleErasedName()} ${fields.joinToString(
        prefix = "[",
        postfix = "]",
        transform = ::toReadableString
    )}"
}

fun ArrayPrimitive.toReadableString(): String {
    return "${value::class.java.simpleErasedName()} ${fields.joinToString(
        prefix = "[",
        postfix = "]",
        transform = ::toReadableString
    )}"
}

fun MapPrimitive.toReadableString(): String {
    return "${value::class.java.simpleErasedName()} ${fields.joinToString(
        prefix = "{",
        postfix = "}",
        transform = ::toReadableString
    )}"
}

fun NullRef.toReadableString(): String = "null"

fun IntPrimitive.toReadableString() = "Int:$value"

fun StringPrimitive.toReadableString() = "String:${if (value == null) null else '"' + value + '"'}"

private fun toReadableString(field: FieldNode) = "${field.fieldName}=${field.value.toReadableString()}"