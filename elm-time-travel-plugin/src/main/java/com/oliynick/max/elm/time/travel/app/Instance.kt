package com.oliynick.max.elm.time.travel.app

import java.lang.reflect.Field
import java.lang.reflect.Type
import javax.swing.tree.DefaultMutableTreeNode
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField

sealed class ObjectNode

data class LevelNode(val of: Any, val type: Type, val value: Collection<ObjectNode>) : ObjectNode()

sealed class Primitive<T> : ObjectNode()

data class IntPrimitive(val value: Int) : Primitive<Int>()

data class StringPrimitive(val value: String) : Primitive<String>()

//////

data class A(val string: String, val compl: Compl)

data class IntB(val value: Int)

data class StringB(val value: String)

data class Compl(val intb: IntB, val strB: StringB, val i: Int)



/*fun Any.toJTree(): DefaultMutableTreeNode {

    fun fillJTree(node: ObjectNode, parent: DefaultMutableTreeNode) {
        val current = DefaultMutableTreeNode(node)
            .also(parent::add)

        if (node is LevelNode) {
            for (j in node.value) {
                fillJTree(j, current)
            }
        }
    }

    val analyzed = traverse(this)

    return DefaultMutableTreeNode(analyzed, true)
        .also { root -> analyzed.value.forEach { node -> fillJTree(node, root) } }
}*/

fun LevelNode.toJTree(): DefaultMutableTreeNode {

    fun fillJTree(node: ObjectNode, parent: DefaultMutableTreeNode) {
        val current = DefaultMutableTreeNode(node)
            .also(parent::add)

        if (node is LevelNode) {
            for (j in node.value) {
                fillJTree(j, current)
            }
        }
    }

    return DefaultMutableTreeNode(this, true)
        .also { root -> value.forEach { node -> fillJTree(node, root) } }
}

fun Any.traverse(): LevelNode {

    fun analyzeField(field: Field, of: Any): ObjectNode {
        field.isAccessible = true

        val value = field.get(of)

        if (value.isPrimitive) {
            return when (value) {
                is String -> StringPrimitive(value)
                is Int -> IntPrimitive(value)
                else -> throw RuntimeException()
            }
        }

        val leafs = value::class.memberProperties
            .asSequence()
            .map { property -> property.javaField }
            .filterNotNull()
            .map { f -> analyzeField(f, value) }
            .toList()

        return LevelNode(value, field.type, leafs)
    }

    val leafs = this::class.memberProperties
        .asSequence()
        .map { property -> property.javaField }
        .filterNotNull()
        .map { analyzeField(it, this) }
        .toList()

    return LevelNode(this, this::class.java, leafs)
}

private val primitives = setOf(
    Int::class, Byte::class, Short::class, Long::class, Double::class,
    Float::class, Char::class, Boolean::class, String::class
)

val Any.isPrimitive: Boolean
    get() = primitives.any { this@isPrimitive::class.isSubclassOf(it) }