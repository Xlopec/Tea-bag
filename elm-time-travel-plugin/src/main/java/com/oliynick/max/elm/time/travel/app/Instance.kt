package com.oliynick.max.elm.time.travel.app

import java.lang.reflect.Field
import java.lang.reflect.Type
import javax.swing.tree.DefaultMutableTreeNode
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.javaField

sealed class Node

data class Instance(val type: Type, val nodes: Collection<Node>) : Node()

data class LevelNode(val type: Type, val fieldName: String, val nodes: Collection<Node>) : Node()

sealed class Primitive<T> : Node() {
    abstract val value: T
}

data class IntPrimitive(override val value: Int) : Primitive<Int>()

data class StringPrimitive(override val value: String) : Primitive<String>()

data class CollectionPrimitive(override val value: Collection<*>,
                               val nodes: Collection<Node>) : Primitive<Collection<*>>()

//////

data class A(val string: String, val compl: Compl)

data class IntB(val value: Int)

data class StringB(val value: String)

data class Compl(val intb: IntB, val strB: StringB, val i: Int)


fun Instance.toJTree(): DefaultMutableTreeNode {

    fun fillJTree(node: Node, parent: DefaultMutableTreeNode) {
        val current = DefaultMutableTreeNode(node)
            .also(parent::add)

        if (node is LevelNode) {
            for (j in node.nodes) {
                fillJTree(j, current)
            }
        }

        if (node is CollectionPrimitive) {
            for (j in node.nodes) {
                fillJTree(j, current)
            }
        }
    }

    return DefaultMutableTreeNode(this, true)
        .also { root -> nodes.forEach { node -> fillJTree(node, root) } }
}

fun Any.traverse() = Instance(this::class.java, collectNodes())

private fun traverse(value: Any, fieldName: String): Node {

    return when (value) {
        is String -> StringPrimitive(value)
        is Int -> IntPrimitive(value)
        is Byte -> TODO()
        is Float -> TODO()
        is Double -> TODO()
        is Long -> TODO()
        is Short -> TODO()
        is Char -> TODO()
        is Boolean -> TODO()
        is Collection<*> -> CollectionPrimitive(value, value.mapIndexedNotNull { index, any -> any?.let { traverse(it, index.toString()) } })
        else -> LevelNode(value::class.java, fieldName, value.collectNodes())
    }
}

private fun Any.collectNodes(): List<Node> {
    return this::class.declaredMemberProperties
        .asSequence()
        .mapNotNull { property -> property.javaField }
        .mapNotNull { field ->
            val value = field.value(this)

            if (value != null) {
                traverse(value, field.name)
            } else null
        }
        .toList()
}

private fun Field.value(of: Any): Any? {
    isAccessible = true
    return get(of)
}