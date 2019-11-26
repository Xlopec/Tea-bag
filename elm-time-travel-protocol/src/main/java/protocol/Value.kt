package protocol

inline class RemoteType(val value: String) {

    constructor(cl: Class<*>) : this(cl.serializeName)

    override fun toString() = "RemoteType(value='$value')"
}

data class Property<T>(val type: RemoteType, val name: String, val v: Value<T>)

sealed class Value<out T> {
    abstract val type: RemoteType
}

sealed class PrimitiveWrapper<T>(val value: T) : Value<T>() {
    final override val type: RemoteType = RemoteType(value!!::class.java)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PrimitiveWrapper<*>) return false

        if (value != other.value) return false
        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        var result = value?.hashCode() ?: 0
        result = 31 * result + type.hashCode()
        return result
    }

    override fun toString() = "PrimitiveWrapper(value=$value, type=$type)"

}

data class Null(override val type: RemoteType) : Value<Any>()

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

sealed class CollectionPrimitiveWrapper<T>(val value: T) : Value<T>() {
    final override val type: RemoteType = RemoteType(value!!::class.java)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CollectionPrimitiveWrapper<*>) return false

        if (value != other.value) return false
        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        var result = value?.hashCode() ?: 0
        result = 31 * result + type.hashCode()
        return result
    }

    override fun toString() = "CollectionPrimitiveWrapper(value=$value, type=$type)"

}

class IterableWrapper(value: Iterable<Value<*>>) : CollectionPrimitiveWrapper<Iterable<Value<*>>>(value)

class MapWrapper(value: Map<Value<*>, Value<*>>) : CollectionPrimitiveWrapper<Map<Value<*>, Value<*>>>(value)

data class Ref(override val type: RemoteType, val properties: Set<Property<*>>) : Value<Any>()

inline val Class<*>.serializeName: String get() = name