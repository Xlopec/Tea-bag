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
) {
    val type: Type = v.type
}

sealed class Value<out T> {
    abstract val type: Type
}

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

data class Null(
    override val type: Type
) : Value<Any>()

class IntWrapper(
    override val type: Type,
    value: Int
) : PrimitiveWrapper<Int>(value)

class ByteWrapper(
    override val type: Type,
    value: Byte
) : PrimitiveWrapper<Byte>(value)

class ShortWrapper(
    override val type: Type,
    value: Short
) : PrimitiveWrapper<Short>(value)

class CharWrapper(
    override val type: Type,
    value: Char
) : PrimitiveWrapper<Char>(value)

class LongWrapper(
    override val type: Type,
    value: Long
) : PrimitiveWrapper<Long>(value)

class DoubleWrapper(
    override val type: Type,
    value: Double
) : PrimitiveWrapper<Double>(value)

class FloatWrapper(
    override val type: Type,
    value: Float
) : PrimitiveWrapper<Float>(value)

class StringWrapper(
    override val type: Type,
    value: String
) : PrimitiveWrapper<String>(value)

class BooleanWrapper(
    override val type: Type,
    value: Boolean
) : PrimitiveWrapper<Boolean>(value)

data class CollectionWrapper(
    override val type: Type,
    val value: List<Value<*>>
) : Value<List<Value<*>>>()

data class Ref(
    override val type: Type,
    val properties: Set<Property<*>>
) : Value<Any>()
