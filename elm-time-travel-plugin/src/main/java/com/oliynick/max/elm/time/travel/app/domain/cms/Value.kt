package com.oliynick.max.elm.time.travel.app.domain.cms

data class Property<T>(val name: String, val v: Value<T>)

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

class IntWrapper(value: Int) : PrimitiveWrapper<Int>(value)

class ByteWrapper(value: Byte) : PrimitiveWrapper<Byte>(value)

class ShortWrapper(value: Short) : PrimitiveWrapper<Short>(value)

class CharWrapper(value: Char) : PrimitiveWrapper<Char>(value)

class LongWrapper(value: Long) : PrimitiveWrapper<Long>(value)

class DoubleWrapper(value: Double) : PrimitiveWrapper<Double>(value)

class FloatWrapper(value: Float) : PrimitiveWrapper<Float>(value)

class StringWrapper(value: String) : PrimitiveWrapper<String>(value)

class BooleanWrapper private constructor(value: Boolean) : PrimitiveWrapper<Boolean>(value) {
    companion object {

        private val TRUE = BooleanWrapper(true)
        private val FALSE = BooleanWrapper(false)

        fun of(value: Boolean) = if (value) TRUE else FALSE
    }
}


//todo remove generic?
sealed class CollectionPrimitiveWrapper<T>(val value: T) : Value<T>() {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CollectionPrimitiveWrapper<*>) return false

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int = value?.hashCode() ?: 0

    override fun toString() = "CollectionPrimitiveWrapper(value=$value)"

}

class CollectionWrapper(value: List<Value<*>>) : CollectionPrimitiveWrapper<List<Value<*>>>(value)

data class Ref(val properties: Set<Property<*>>) : Value<Any>()
