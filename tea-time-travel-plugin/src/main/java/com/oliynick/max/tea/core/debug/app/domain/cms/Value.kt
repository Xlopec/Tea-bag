package com.oliynick.max.tea.core.debug.app.domain.cms

inline class Type(
    val name: String
) {
    companion object {
        fun of(
            name: String
        ): Type {
            require(name.isNotEmpty())
            return Type(name)
        }

        fun of(
            any: Any
        ) = of(any::class.java.name!!)
    }
}

data class Property(
    val name: String,
    val v: Value
)

sealed class Value

object Null : Value()

//todo replace by overloaded factory function

class IntWrapper(
    val value: Int
) : Value()

class ByteWrapper(
    val value: Byte
) : Value()

class ShortWrapper(
    val value: Short
) : Value()

class CharWrapper(
    val value: Char
) : Value()

class LongWrapper(
    val value: Long
) : Value()

class DoubleWrapper(
    val value: Double
) : Value()

class FloatWrapper(
    val value: Float
) : Value()

class StringWrapper(
    val value: String
) : Value()

class BooleanWrapper private constructor(
    val value: Boolean
) : Value() {

    companion object {
        private val TRUE by lazy(LazyThreadSafetyMode.NONE) { BooleanWrapper(true) }
        private val FALSE by lazy(LazyThreadSafetyMode.NONE) { BooleanWrapper(false) }

        fun of(
            value: Boolean
        ) = if (value) TRUE else FALSE
    }
}

data class CollectionWrapper(
    val value: List<Value>
) : Value()

data class Ref(
    val type: Type,
    val properties: Set<Property>
) : Value()

inline val Value.isPrimitive: Boolean
    get() = when (this) {
        is IntWrapper,
        is ByteWrapper,
        is ShortWrapper,
        is CharWrapper,
        is LongWrapper,
        is DoubleWrapper,
        is FloatWrapper,
        is StringWrapper,
        is BooleanWrapper -> true
        Null, is CollectionWrapper, is Ref -> false
    }

