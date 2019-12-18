package protocol

@DslMarker
private annotation class DslBuilder

@DslBuilder
class RefSerializer<T>(val t: T) {

    @PublishedApi
    internal val properties = mutableSetOf<Property<*>>()

    inline fun <reified T> T.property(name: String, block: T.() -> Value<*>) {
        properties += Property(RemoteType(T::class.java), name, block(this))
    }

    infix fun String.of(p: Property<*>) {
        properties += p
    }

    infix fun String.of(block: T.() -> Value<*>) {
        val value = t.run(block)

        properties += Property(value.type, this, value)
    }

    fun <V> String.of(it: Iterable<V>, block: V.() -> Value<*>) {
        properties += Property(RemoteType(it::class.java), this, CollectionWrapper(it.map(block)))
    }

    infix fun String.of(s: String?) {
        properties += Property(RemoteType(String::class.java), this, s?.let { wrap(it) } ?: wrap(String::class.java))
    }

    infix fun String.of(r: Ref) {
        properties += Property(r.type, this, r)
    }

    inline infix fun <reified T> T.ref(block: RefSerializer<T>.() -> Unit): Ref =
        Ref(RemoteType(T::class.java), RefSerializer(this).apply(block).properties)

}

typealias FromVal<V, T> = (V) -> T

@DslBuilder
class RefDeserializer(val ref: Ref) {

    @JvmName("nonNullRef")
    infix fun <T> String.nonNull(block: FromVal<Ref, T>): T = block(nonNull<Ref>(ref, this))

    inline infix fun <T> String.nullable(block: FromVal<Ref, T>): T? = nullable<Ref>(ref, this)?.let(block)

    @JvmName("nonNullString")
    infix fun <T> String.nonNull(block: FromVal<String, T>): T = block(nonNull<StringWrapper>(ref, this).value)

    @JvmName("nonNullCollectionWrapper")
    infix fun <T> String.nonNull(block: FromVal<CollectionWrapper, T>): T = block(nonNull<CollectionWrapper>(ref, this))

    @JvmName("nullableString")
    infix fun <T> String.nullable(block: FromVal<String, T>): T? =
        nullable<StringWrapper>(ref, this)?.value?.let(block)

}

inline infix fun <reified T> T.toRef(block: T.() -> Ref): Ref = run(block)

inline infix fun <reified T> T.ref(block: RefSerializer<T>.() -> Unit): Ref =
    Ref(RemoteType(T::class.java), RefSerializer(this).apply(block).properties)

inline infix fun <reified T> Ref.nonNull(block: RefDeserializer.() -> T): T = RefDeserializer(this).run(block)

@PublishedApi
internal fun Ref.property(name: String) =
    properties.find { prop -> prop.name == name } ?: notifyPropertyNotFound(this, name)

@PublishedApi
internal inline fun <reified V : Value<*>> nullable(
    ref: Ref,
    name: String
): V? = ref.property(name).v
        .also { v ->
            check(v is Null || v is V) { "value is neither ${V::class.java} nor ${Null::class.java}" }
        } as? V

@PublishedApi
internal inline fun <reified V : Value<*>> nonNull(
    ref: Ref,
    name: String
): V = nullable<V>(ref, name) ?: error("Non nullable property of type ${V::class.java} was null, property: ${ref.property(name)}")

private fun notifyPropertyNotFound(
    ref: Ref,
    name: String
): Nothing = error(
    "Couldn't find property named $name, " +
        "ref of type ${ref.type.value} contains " +
        "the following properties: ${ref.properties.joinToString { it.name }}"
)
