package com.oliynick.max.elm.time.travel.app.model

import java.lang.reflect.Field
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField

sealed class Leaf

data class Complex(val of: Any, val value: Collection<Leaf>) : Leaf()

sealed class Primitive<T> : Leaf()

data class IntPrimitive(val value: Int) : Primitive<Int>()

data class StringPrimitive(val value: String) : Primitive<String>()

//////

data class A(val string: String, val compl: Compl)

data class IntB(val value: Int)

data class StringB(val value: String)

data class Compl(val intb: IntB, val strB: StringB, val i: Int)

fun main() {

    val a = A("max", Compl(IntB(124), StringB("kek"), 1488))

    val anal = analyze(a)

    println(anal)
}

fun Primitive<*>.update(with: String) {

}

fun analyze(obj: Any): Complex {

    fun analyzeField(field: Field, of: Any): Leaf {
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

        return Complex(value, leafs)
    }

    val leafs = obj::class.memberProperties
        .asSequence()
        .map { property -> property.javaField }
        .filterNotNull()
        .map { analyzeField(it, obj) }
        .toList()

    return Complex(obj, leafs)
}

private val primitives = setOf(Int::class, Byte::class, Short::class, Long::class, Double::class,
    Float::class, Char::class, Boolean::class, String::class)

val Any.isPrimitive: Boolean
    get() = primitives.any { this@isPrimitive::class.isSubclassOf(it) }