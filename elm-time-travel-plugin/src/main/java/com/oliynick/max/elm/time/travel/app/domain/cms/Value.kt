package com.oliynick.max.elm.time.travel.app.domain.cms

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

data class Property<T>(
    val name: String,
    val v: Value<T>
)

sealed class Value<out T>

sealed class PrimitiveWrapper<T>(val value: T) : Value<T>() {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PrimitiveWrapper<*>) return false

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        return value?.hashCode() ?: 0
    }

    override fun toString() = "PrimitiveWrapper(value=$value)"

}

object Null : Value<Any>()

class IntWrapper(
    value: Int
) : PrimitiveWrapper<Int>(value)

class ByteWrapper(
    value: Byte
) : PrimitiveWrapper<Byte>(value)

class ShortWrapper(
    value: Short
) : PrimitiveWrapper<Short>(value)

class CharWrapper(
    value: Char
) : PrimitiveWrapper<Char>(value)

class LongWrapper(
    value: Long
) : PrimitiveWrapper<Long>(value)

class DoubleWrapper(
    value: Double
) : PrimitiveWrapper<Double>(value)

class FloatWrapper(
    value: Float
) : PrimitiveWrapper<Float>(value)

class StringWrapper(
    value: String
) : PrimitiveWrapper<String>(value)

class BooleanWrapper(
    value: Boolean
) : PrimitiveWrapper<Boolean>(value)

data class CollectionWrapper(
    val value: List<Value<*>>
) : Value<List<Value<*>>>()

data class Ref(
    val type: Type,
    val properties: Set<Property<*>>
) : Value<Any>()
