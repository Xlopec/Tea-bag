package com.oliynick.max.tea.core.debug.app.domain

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

data class IntWrapper(
    val value: Int
) : Value()

data class ByteWrapper(
    val value: Byte
) : Value()

data class ShortWrapper(
    val value: Short
) : Value()

data class CharWrapper(
    val value: Char
) : Value()

data class LongWrapper(
    val value: Long
) : Value()

data class DoubleWrapper(
    val value: Double
) : Value()

data class FloatWrapper(
    val value: Float
) : Value()

data class StringWrapper(
    val value: String
) : Value()

class BooleanWrapper private constructor(
    val value: Boolean
) : Value() {

    companion object {
        private val TRUE by lazy(LazyThreadSafetyMode.NONE) {
            BooleanWrapper(
                true
            )
        }
        private val FALSE by lazy(LazyThreadSafetyMode.NONE) {
            BooleanWrapper(
                false
            )
        }

        fun of(
            value: Boolean
        ) = if (value) TRUE else FALSE
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BooleanWrapper

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun toString(): String {
        return "BooleanWrapper(value=$value)"
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
        is BooleanWrapper
        -> true
        Null, is CollectionWrapper, is Ref -> false
    }

