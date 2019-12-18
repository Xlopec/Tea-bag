@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package protocol

import java.util.*
import kotlin.collections.LinkedHashSet

interface Converter<T, V : Value<*>> {

    fun from(v: V, converters: Converters): T?

    fun to(t: T, converters: Converters): V
}

fun converters(
    config: Converters.() -> Unit = {}
): Converters =
    Converters()
        .apply {
            +RefConverter
            +IterableConverter
            +ListConverter
            +SetConverter
            +CollectionConverter
            +QueueConverter
            +DequeueConverter
            +MapConverter
            +StringConverter
            register(IntConverter, Integer::class.java, Int::class.java)
            register(ByteConverter, java.lang.Byte::class.java, Byte::class.java)
            register(ShortConverter, java.lang.Short::class.java, Short::class.java)
            register(CharConverter, Character::class.java, Char::class.java)
            register(LongConverter, java.lang.Long::class.java, Long::class.java)
            register(DoubleConverter, java.lang.Double::class.java, Double::class.java)
            register(FloatConverter, java.lang.Float::class.java, Float::class.java)
            register(BooleanConverter, java.lang.Boolean::class.java, Boolean::class.java)
        }.apply(config)

class Converters internal constructor() {

    @PublishedApi
    internal val converters = mutableMapOf<String, Converter<Any, Value<*>>>()

    inline operator fun <reified T, V : Value<*>> Converter<T, V>.unaryPlus() {
        @Suppress("UNCHECKED_CAST")
        converters[T::class.java.key] = this as Converter<Any, Value<*>>
    }

    inline operator fun <reified T> T.unaryMinus() {
        converters.remove(T::class.java.key)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T, V : Value<*>> register(
        converter: Converter<T, V>,
        cl: Class<out T>,
        vararg other: Class<out T>
    ) {
        register(converter, cl)
        other.forEach { c -> register(converter, c) }
    }

    fun <T, V : Value<*>> register(converter: Converter<T, V>, cl: Class<out T>) {
        @Suppress("UNCHECKED_CAST")
        converters[cl.key] = converter as Converter<Any, Value<*>>
    }

    @Suppress("UNCHECKED_CAST")
    internal fun <N, W : Any, V : Value<*>> register(
        converter: Converter<N, V>,
        wrapper: Class<out W>,
        primitive: Class<out N>
    ) {
        val cast = converter as Converter<Any, Value<*>>

        converters[wrapper.key] = cast
        converters[primitive.key] = cast
    }

    operator fun get(remoteType: RemoteType) = converters[remoteType.value]

    internal operator fun get(cl: Class<*>) = converters[cl.key]

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Converters

        if (converters != other.converters) return false

        return true
    }

    override fun hashCode(): Int {
        return converters.hashCode()
    }

    override fun toString(): String {
        return "Converters(converters=$converters)"
    }


    @PublishedApi
    internal inline val Class<*>.key: String
        get() = canonicalName


}

private object IntConverter : Converter<Int, IntWrapper> {
    override fun from(v: IntWrapper, converters: Converters) = v.value
    override fun to(t: Int, converters: Converters) =
        wrap(t)
}

private object ByteConverter : Converter<Byte, ByteWrapper> {
    override fun from(v: ByteWrapper, converters: Converters) = v.value
    override fun to(t: Byte, converters: Converters) =
        wrap(t)
}

private object ShortConverter : Converter<Short, ShortWrapper> {
    override fun from(v: ShortWrapper, converters: Converters) = v.value
    override fun to(t: Short, converters: Converters) =
        wrap(t)
}

private object CharConverter : Converter<Char, CharWrapper> {
    override fun from(v: CharWrapper, converters: Converters) = v.value
    override fun to(t: Char, converters: Converters) =
        wrap(t)
}

private object LongConverter : Converter<Long, LongWrapper> {
    override fun from(v: LongWrapper, converters: Converters) = v.value
    override fun to(t: Long, converters: Converters) =
        wrap(t)
}

private object DoubleConverter : Converter<Double, DoubleWrapper> {
    override fun from(v: DoubleWrapper, converters: Converters) = v.value
    override fun to(t: Double, converters: Converters) =
        wrap(t)
}

private object FloatConverter : Converter<Float, FloatWrapper> {
    override fun from(v: FloatWrapper, converters: Converters) = v.value
    override fun to(t: Float, converters: Converters) =
        wrap(t)
}

private object BooleanConverter : Converter<Boolean, BooleanWrapper> {
    override fun from(v: BooleanWrapper, converters: Converters) = v.value
    override fun to(t: Boolean, converters: Converters) =
        wrap(t)
}

private object StringConverter : Converter<String, StringWrapper> {
    override fun from(v: StringWrapper, converters: Converters) = v.value
    override fun to(t: String, converters: Converters) = wrap(t)
}

private object IterableConverter : Converter<Iterable<*>, CollectionWrapper> {
    override fun from(v: CollectionWrapper, converters: Converters): Iterable<*> {
        return v.value.map { elem -> elem.fromValue(converters) }
    }

    override fun to(t: Iterable<*>, converters: Converters) = wrap(t, converters)
}

private object ListConverter : Converter<List<*>, CollectionWrapper> {
    override fun from(v: CollectionWrapper, converters: Converters): List<*> {
        return v.value.map { elem -> elem.fromValue(converters) }
    }

    override fun to(t: List<*>, converters: Converters) = wrap(t, converters)
}

private object QueueConverter : Converter<Queue<*>, CollectionWrapper> {
    override fun from(v: CollectionWrapper, converters: Converters): Queue<*> {
        return v.value.mapTo(LinkedList()) { elem -> elem.fromValue(converters) }
    }

    override fun to(t: Queue<*>, converters: Converters) =
        wrap(t, converters)
}

private object DequeueConverter : Converter<Deque<*>, CollectionWrapper> {
    override fun from(v: CollectionWrapper, converters: Converters): Deque<*> {
        return v.value.mapTo(ArrayDeque(v.value.size)) { elem -> elem.fromValue(converters) }
    }

    override fun to(t: Deque<*>, converters: Converters) =
        wrap(t, converters)
}

private object CollectionConverter : Converter<Collection<*>, CollectionWrapper> {
    override fun from(v: CollectionWrapper, converters: Converters) =
        ListConverter.from(v, converters)

    override fun to(t: Collection<*>, converters: Converters) = wrap(t, converters)
}

private object SetConverter : Converter<Set<*>, CollectionWrapper> {
    override fun from(v: CollectionWrapper, converters: Converters): Set<*> =
        v.value.mapTo(LinkedHashSet(v.value.size)) { elem -> elem.fromValue(converters) }

    override fun to(t: Set<*>, converters: Converters) = wrap(t, converters)
}

private object MapConverter : Converter<Map<*, *>, MapWrapper> {
    override fun from(v: MapWrapper, converters: Converters): Map<*, *> =
        v.value.entries.associate { e -> e.key.fromValue(converters) to e.value.fromValue(converters) }

    override fun to(t: Map<*, *>, converters: Converters) = wrap(t, converters)
}

private object RefConverter : Converter<Any, Ref> {
    override fun from(v: Ref, converters: Converters) = v.fromRefValue(converters)
    override fun to(t: Any, converters: Converters) = wrap(t, converters)
}
