@file:Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")

package protocol

import sun.misc.Unsafe
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import kotlin.reflect.KClass
import kotlin.reflect.full.allSuperclasses
import kotlin.reflect.full.isSuperclassOf

private val classComparator = Comparator<KClass<*>> { t1, t2 ->
    when {
        t2 == t1 -> 0
        t1.isSuperclassOf(t2) -> 1
        else -> -1
    }
}

@Suppress("UNCHECKED_CAST")
fun <T> T?.toValue(
    cl: Class<out T>,
    converters: Converters
): Value<T> =
    (this?.let { t -> converters.findConverter(cl).to(t, converters) } ?: wrap(cl)) as Value<T>

@Suppress("UNCHECKED_CAST")
fun <T> T.toValue(converters: Converters): Value<T> = toValue(clazz, converters) as Value<T>

@Suppress("UNCHECKED_CAST")
fun <T> Value<T>.fromValue(converters: Converters): T? = when (this) {
    is Null -> null
    is Ref,
    is PrimitiveWrapper<*>,
    is CollectionPrimitiveWrapper<*> -> converters.findConverter(type).from(this, converters)
} as T?

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
inline fun wrap(v: Iterable<*>, converters: Converters): CollectionWrapper {
    return CollectionWrapper(v.map { it.toValue(it.clazz, converters) })
}

@Suppress("NOTHING_TO_INLINE")
inline fun wrap(cl: Class<out Any>) = Null(RemoteType(cl))

@Suppress("NOTHING_TO_INLINE")
inline fun wrap(any: Any, converters: Converters): Ref {

    require(!any::class.java.isPrimitive) { "Not a reference type, was $any (${any.javaClass})" }

    val properties = collectFields(any::class.java)
        .onEach { field -> field.isAccessible = true }
        .map { field ->

            val value: Any? = field.get(any)
            // we want to store an actual class, not the declared
            // one if possible
            val type = value?.javaClass ?: field.type

            Property(
                RemoteType(type),
                field.name,
                value.toValue(type, converters)
            )
        }
        .toSet()

    return Ref(RemoteType(any::class.java), properties)
}

/**
 * Collects all reachable fields recursively
 * while ignoring static and transient fields
 */
@PublishedApi
internal fun collectFields(
    clazz: Class<*>
): Sequence<Field> =
    (clazz.declaredFields.asSequence() + (clazz.superclass?.let(::collectFields) ?: emptySequence()))
        .filter { f -> !f.isTransient && !f.isStatic }

internal fun Ref.fromRefValue(
    converters: Converters
): Any {
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

private fun Converters.findConverter(
    type: RemoteType
): Converter<Any, Value<*>> = this[type] ?: findConverter(type.clazz)

private fun Converters.findConverter(
    cl: Class<*>
): Converter<Any, Value<*>> =
    // fixme
    this[cl] ?: cl.kotlin.allSuperclasses
        .asSequence()
        .sortedWith(classComparator)
        .mapNotNull { c -> this[c.java] }
        .toList()
        .also { suitableConverters -> require(suitableConverters.isNotEmpty()) { "Couldn't find a suitable converter for class $cl" } }
        .first()

private fun Unsafe.fill(
    instance: Any,
    property: Property<*>,
    cl: Class<*>,
    converters: Converters
) {

    val field = instance.getFieldFor(property)
    val offset = objectFieldOffset(field)

    when (val value = property.getValue(converters)) {
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

private fun Property<*>.getValue(
    converters: Converters
): Any? = if (v is Null) null else converters.findConverter(type).from(v, converters)

private fun Any.getFieldFor(
    property: Property<*>
): Field {

    fun find(cl: Class<*>): Field {
        return cl.declaredFields.find { f -> f.name == property.name }
            ?: find((cl.superclass ?: notifyUnknownField(this@getFieldFor, property)))
    }

    return find(this::class.java)
}

private fun notifyUnknownField(
    instance: Any,
    property: Property<*>
): Nothing = error(
    "Couldn't find field with name ${property.name}" +
        "\nfor instance ${instance},\nproperty $property"
)

private inline val RemoteType.clazz
    get() = Class.forName(value)

private val unsafe: Unsafe by lazy {
    val f = Unsafe::class.java.getDeclaredField("theUnsafe")
        .also { field -> field.isAccessible = true }

    f.get(null) as Unsafe
}

private inline val Field.isTransient get() = Modifier.isTransient(modifiers)

private inline val Field.isStatic get() = Modifier.isStatic(modifiers)

@PublishedApi
internal inline val Any?.clazz
    get() = if (this == null) Nothing::class.java else this::class.java