@file:Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")

package protocol

import sun.misc.Unsafe
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import kotlin.Boolean
import kotlin.Byte
import kotlin.Char
import kotlin.Double
import kotlin.Float
import kotlin.Int
import kotlin.Long
import kotlin.Short
import kotlin.reflect.KClass
import kotlin.reflect.full.allSuperclasses
import kotlin.reflect.full.isSuperclassOf
import java.lang.Boolean as JBooleanWrapper
import java.lang.Byte as JByteWrapper
import java.lang.Character as JCharacterWrapper
import java.lang.Double as JDoubleWrapper
import java.lang.Float as JFloatWrapper
import java.lang.Integer as JIntWrapper
import java.lang.Long as JLongWrapper
import java.lang.Short as JShortWrapper

private val classComparator = Comparator<KClass<*>> { t1, t2 ->
    when {
        t2 == t1 -> 0
        t1.isSuperclassOf(t2) -> 1
        else -> -1
    }
}

interface Converter<T, V : Value<*>> {

    fun from(v: V, converters: Converters): T?

    fun to(t: T, converters: Converters): V
}

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
    override fun to(t: Int, converters: Converters) = wrap(t)
}

private object ByteConverter : Converter<Byte, ByteWrapper> {
    override fun from(v: ByteWrapper, converters: Converters) = v.value
    override fun to(t: Byte, converters: Converters) = wrap(t)
}

private object ShortConverter : Converter<Short, ShortWrapper> {
    override fun from(v: ShortWrapper, converters: Converters) = v.value
    override fun to(t: Short, converters: Converters) = wrap(t)
}

private object CharConverter : Converter<Char, CharWrapper> {
    override fun from(v: CharWrapper, converters: Converters) = v.value
    override fun to(t: Char, converters: Converters) = wrap(t)
}

private object LongConverter : Converter<Long, LongWrapper> {
    override fun from(v: LongWrapper, converters: Converters) = v.value
    override fun to(t: Long, converters: Converters) = wrap(t)
}

private object DoubleConverter : Converter<Double, DoubleWrapper> {
    override fun from(v: DoubleWrapper, converters: Converters) = v.value
    override fun to(t: Double, converters: Converters) = wrap(t)
}

private object FloatConverter : Converter<Float, FloatWrapper> {
    override fun from(v: FloatWrapper, converters: Converters) = v.value
    override fun to(t: Float, converters: Converters) = wrap(t)
}

private object BooleanConverter : Converter<Boolean, BooleanWrapper> {
    override fun from(v: BooleanWrapper, converters: Converters) = v.value
    override fun to(t: Boolean, converters: Converters) = wrap(t)
}

private object StringConverter : Converter<String, StringWrapper> {
    override fun from(v: StringWrapper, converters: Converters) = v.value
    override fun to(t: String, converters: Converters) = wrap(t)
}

//TODO add converter for specific collections
private object IterableConverter : Converter<Iterable<*>, IterableWrapper> {
    override fun from(v: IterableWrapper, converters: Converters): Iterable<*> {
        return v.value.map { elem -> elem.fromValue(converters) }
    }

    override fun to(t: Iterable<*>, converters: Converters) = wrap(t, converters)
}

private object ListConverter : Converter<List<*>, IterableWrapper> {
    override fun from(v: IterableWrapper, converters: Converters): List<*> {
        return v.value.map { elem -> elem.fromValue(converters) }
    }

    override fun to(t: List<*>, converters: Converters) = wrap(t, converters)
}

private object ArrayListConverter : Converter<ArrayList<*>, IterableWrapper> {
    override fun from(v: IterableWrapper, converters: Converters): ArrayList<*> {
        return v.value.mapTo(
            ArrayList(
                (v.value as? Collection<*>)?.size ?: 10
            )
        ) { elem -> elem.fromValue(converters) }
    }

    override fun to(t: ArrayList<*>, converters: Converters) = wrap(t, converters)
}

private object MapConverter : Converter<Map<*, *>, MapWrapper> {
    override fun from(v: MapWrapper, converters: Converters): Map<*, *> {
        return v.value.entries.associate { e ->
            e.key.fromValue(converters) to e.value.fromValue(
                converters
            )
        }
    }

    override fun to(t: Map<*, *>, converters: Converters) = wrap(t, converters)
}

private object RefConverter : Converter<Any, Ref> {
    override fun from(v: Ref, converters: Converters) = v.fromValue(converters)
    override fun to(t: Any, converters: Converters) = wrap(t, converters)
}

fun converters(config: Converters.() -> Unit = {}): Converters {
    return Converters()
        .apply {
            +RefConverter
            +IterableConverter
            +ListConverter
            +ArrayListConverter
            +MapConverter
            +StringConverter
            register(IntConverter, JIntWrapper::class.java, Int::class.java)
            register(ByteConverter, JByteWrapper::class.java, Byte::class.java)
            register(ShortConverter, JShortWrapper::class.java, Short::class.java)
            register(CharConverter, JCharacterWrapper::class.java, Char::class.java)
            register(LongConverter, JLongWrapper::class.java, Long::class.java)
            register(DoubleConverter, JDoubleWrapper::class.java, Double::class.java)
            register(FloatConverter, JFloatWrapper::class.java, Float::class.java)
            register(BooleanConverter, JBooleanWrapper::class.java, Boolean::class.java)
        }.apply(config)
}

fun <T> T?.toValue(
    cl: Class<out T>,
    converters: Converters
): Value<T> {

    @Suppress("UNCHECKED_CAST")
    return (this?.let { t -> converters.findSuitableConverter(cl).to(t, converters) }
        ?: wrap(cl)) as Value<T>
}

@Suppress("UNCHECKED_CAST")
fun <T> T.toValue(converters: Converters): Value<T> = toValue(clazz, converters) as Value<T>

@Suppress("UNCHECKED_CAST")
fun <T> Value<T>.fromValue(converters: Converters): T? = when (this) {
    is Null -> null
    is Ref,
    is PrimitiveWrapper<*>,
    is CollectionPrimitiveWrapper<*> -> converters.findSuitableConverter(type).from(
        this,
        converters
    )
} as T?

//todo rename to wrap|val|primitive?
@Suppress("NOTHING_TO_INLINE")
inline fun wrap(v: Int) = IntWrapper(v)

@Suppress("NOTHING_TO_INLINE")
inline fun wrap(v: Byte) = ByteWrapper(v)

@Suppress("NOTHING_TO_INLINE")
inline fun wrap(v: Short) = ShortWrapper(v)

@Suppress("NOTHING_TO_INLINE")
inline fun wrap(v: Char) = CharWrapper(v)

@Suppress("NOTHING_TO_INLINE")
inline fun wrap(v: Long) = LongWrapper(v)

@Suppress("NOTHING_TO_INLINE")
inline fun wrap(v: Float) = FloatWrapper(v)

@Suppress("NOTHING_TO_INLINE")
inline fun wrap(v: Double) = DoubleWrapper(v)

@Suppress("NOTHING_TO_INLINE")
inline fun wrap(v: Boolean) = BooleanWrapper.of(v)

@Suppress("NOTHING_TO_INLINE")
inline fun wrap(v: String) = StringWrapper(v)

fun wrap(v: Map<*, *>, converters: Converters): MapWrapper {

    fun Any?.toValue() = toValue(this.clazz, converters)

    return MapWrapper(v.entries.associate { e -> e.key.toValue() to e.value.toValue() })
}

@Suppress("NOTHING_TO_INLINE")
inline fun wrap(v: Iterable<*>, converters: Converters): IterableWrapper {
    return IterableWrapper(v.map { it.toValue(it.clazz, converters) })
}

@Suppress("NOTHING_TO_INLINE")
inline fun wrap(cl: Class<out Any>) = Null(RemoteType(cl))

@Suppress("NOTHING_TO_INLINE")
inline fun wrap(any: Any, converters: Converters): Ref {

    require(!any::class.java.isPrimitive) { "Not a reference type, was $any (${any.javaClass})" }

    val properties = collectFields(any::class.java)
        .onEach { field -> field.isAccessible = true }
        .map { field ->
            Property(
                RemoteType(field.type),
                field.name,
                field.get(any).toValue(field.type, converters)
            )
        }
        .toSet()

    return Ref(RemoteType(any::class.java), properties)
}

@PublishedApi
internal inline val Any?.clazz
    get() = if (this == null) Nothing::class.java else this::class.java

/**
 * Collects all reachable fields recursively
 */
@PublishedApi
internal fun collectFields(clazz: Class<*>): Sequence<Field> {
    return (clazz.declaredFields.asSequence() + (clazz.superclass?.let(::collectFields)
        ?: emptySequence()))
        .filter { f -> !f.isTransient && !f.isStatic }
}

private inline val RemoteType.clazz
    get() = Class.forName(value)

private val unsafe: Unsafe by lazy {
    val f = Unsafe::class.java.getDeclaredField("theUnsafe")
        .also { field -> field.isAccessible = true }

    f.get(null) as Unsafe
}

private fun Ref.fromValue(converters: Converters): Any {
    val cl = type.clazz

    return unsafe.allocateInstance(cl)
        .also { instance ->
            properties.forEach { property ->
                unsafe.fill(
                    instance,
                    property,
                    cl,
                    converters
                )
            }
        }
}

private fun Converters.findSuitableConverter(type: RemoteType): Converter<Any, Value<*>> {
    return this[type] ?: findSuitableConverter(type.clazz)
}

private fun Converters.findSuitableConverter(cl: Class<*>): Converter<Any, Value<*>> {
    return this[cl] ?: cl.kotlin.allSuperclasses
        .asSequence()
        .sortedWith(classComparator)
        .mapNotNull { c -> this[c.java] }
        .toList()
        .also { suitableConverters -> require(suitableConverters.isNotEmpty()) { "Couldn't find a suitable converter for class $cl" } }
        .first()
}

private fun Unsafe.fill(
    instance: Any,
    property: Property<*>,
    cl: Class<*>,
    converters: Converters
) {

    val field = instance.getFieldFor(property)
    val offset = objectFieldOffset(field)

    when (val value =
        if (property.v is Null) null else converters.findSuitableConverter(property.type).from(
            property.v,
            converters
        )) {
        is Int -> putInt(instance, offset, value)
        is Byte -> putByte(instance, offset, value)
        is Short -> putShort(instance, offset, value)
        is Boolean -> putBoolean(instance, offset, value)
        is Long -> putLong(instance, offset, value)
        is Double -> putDouble(instance, offset, value)
        is Float -> putFloat(instance, offset, value)
        is Char -> putChar(instance, offset, value)
        is Any, null -> putObject(instance, offset, value)
        else -> error(
            "Should never happen, couldn't convert value $value\nof instance (class=$cl) $instance,\n" +
                "registered converters $converters"
        )
    }
}

private fun Any.getFieldFor(property: Property<*>): Field {

    fun find(cl: Class<*>): Field {
        return cl.declaredFields.find { f -> f.name == property.name }
            ?: find((cl.superclass ?: notifyUnknownField(this@getFieldFor, property)))
    }

    return find(this::class.java)
}

private inline val Field.isTransient get() = Modifier.isTransient(modifiers)

private inline val Field.isStatic get() = Modifier.isStatic(modifiers)

private fun notifyUnknownField(instance: Any, property: Property<*>): Nothing {
    error(
        "Couldn't find field with name ${property.name}" +
            "\nfor instance ${instance},\nproperty $property"
    )
}
