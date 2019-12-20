@file:Suppress("FunctionName")

package com.max.weatherviewer.app

import com.google.gson.*
import com.max.weatherviewer.app.env.Environment
import com.max.weatherviewer.app.serialization.PersistentListSerializer
import com.max.weatherviewer.app.serialization.Serializer
import com.max.weatherviewer.screens.feed.Feed
import com.max.weatherviewer.screens.feed.FeedLoading
import com.max.weatherviewer.screens.feed.LoadCriteria
import com.oliynick.max.elm.core.actor.Component
import com.oliynick.max.elm.core.component.Component
import com.oliynick.max.elm.core.component.Env
import com.oliynick.max.elm.core.component.androidLogger
import com.oliynick.max.elm.time.travel.Component
import com.oliynick.max.elm.time.travel.URL
import com.oliynick.max.elm.time.travel.gsonSerializer
import com.oliynick.max.elm.time.travel.registerReflectiveTypeAdapter
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import protocol.ComponentId
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.*

fun Environment.appComponent(): Component<Message, State> {

    suspend fun resolve(command: Command) = this.resolve(command)

    fun update(
        message: Message,
        state: State
    ) = this.update(message, state)

    val initScreen = FeedLoading(
        UUID.randomUUID(),
        LoadCriteria.Query("android")
    )

    // todo state persistence
    val componentDependencies = Env(
        State(initScreen),
        ::resolve,
        ::update,
        LoadByCriteria(
            initScreen.id,
            initScreen.criteria
        )
    ) {
        interceptor = androidLogger("News Reader App")
    }

    if (isDebug) {

        return Component(ComponentId("News Reader App"), componentDependencies, URL(host = "10.0.2.2")) {
            serverSettings {
                installSerializer(AppGsonSerializer())
            }
        }
    }

    return Component(componentDependencies)

}

fun AppGsonSerializer() = gsonSerializer {

    registerTypeHierarchyAdapter(PersistentList::class.java, PersistentListSerializer)
    registerReflectiveTypeAdapter<Feed>()
    registerReflectiveTypeAdapter<LoadCriteria>()

}

inline fun <reified T : Any> GsonBuilder.registerAdapter() {
    registerTypeAdapter(T::class.java, polyAdapter<T>())
}

inline fun <reified T : Any> GsonBuilder.registerHierarchyAdapter() {
    registerTypeHierarchyAdapter(T::class.java, polyAdapter<T>())
}

private object PersistentListAdapter : Serializer<PersistentList<*>> {

    override fun serialize(
        src: PersistentList<*>,
        typeOfSrc: Type,
        context: JsonSerializationContext
    ) = context.typedJsonObject(src, (typeOfSrc as ParameterizedType).actualTypeArguments[0] as Class<*>)

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext
    ): PersistentList<*> = context.deserializeTypedJsonArray(json).toPersistentList()

}

private object ListAdapter : Serializer<List<*>> {

    override fun serialize(
        src: List<*>,
        typeOfSrc: Type,
        context: JsonSerializationContext
    ) = context.typedJsonObject(src, (typeOfSrc as ParameterizedType).actualTypeArguments[0] as Class<*>)

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext
    ): List<*> = context.deserializeTypedJsonArray(json)

}

inline fun <reified T : Any> polyAdapter() = object : Serializer<T> {
    override fun serialize(
        src: T,
        typeOfSrc: Type?,
        context: JsonSerializationContext
    ): JsonElement {
        println("ser $src")
        return context.typedJsonObject(src)
    }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext
    ): T = context.deserializeTypedJson(json)

}

fun polyAdapter(cl: Class<*>) = object : Serializer<Any> {
    override fun serialize(
        src: Any,
        typeOfSrc: Type?,
        context: JsonSerializationContext
    ): JsonElement {
        println("ser $src")
        return context.typedJsonObject(src, cl)
    }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext
    ): Any = context.deserializeTypedJson(json)

}

private object MessageAdapter : JsonDeserializer<Message> {

    /*override fun serialize(
        src: Message,
        typeOfSrc: Type,
        context: JsonSerializationContext
    ) = context.typedJsonObject(src)*/

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext
    ): Message = context.deserializeTypedJson(json)

}

private object AnyAdapter : Serializer<Any> {

    override fun serialize(
        src: Any,
        typeOfSrc: Type,
        context: JsonSerializationContext
    ) = context.typedJsonObject(src)

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext
    ): Any = context.deserializeTypedJson(json)

}

val Type.serializeValue: String
    get() = (this as Class<*>).canonicalName!!


fun ClassFor(className: String) = Class.forName(className)

inline fun <reified T> JsonDeserializationContext.deserializeTypedJson(
    json: JsonElement
): T = json.asJsonObject.let { obj ->

    val typeOf = ClassFor(obj["type"].asString) as Class<out T>

    deserialize(obj["value"], typeOf)
}

fun JsonDeserializationContext.deserializeTypedJsonArray(
    json: JsonElement
): List<*> = json.asJsonObject.let { obj ->

    val typeOf = ClassFor(obj["type"].asString)

    obj["elements"].asJsonArray.map { elem -> deserialize<Any?>(elem, typeOf) }
}

private fun Any.typedJsonObject(
    type: Class<*> = this::class.java,
    config: JsonObject.() -> Unit
): JsonObject = JsonObject()
    .apply {
        addProperty("type", type.serializeValue)
    }
    .apply(config)

fun JsonSerializationContext.typedJsonObject(
    any: Any,
    type: Class<*> = any::class.java
): JsonObject = JsonObject()
    .apply {
        addProperty("type", type.serializeValue)
        add("value", serialize(any, type))
    }

fun JsonSerializationContext.typedJsonObject(
    it: Iterable<*>,
    type: Class<*>
): JsonObject = JsonObject()
    .apply {
        add(
            "elements",
            it.fold(JsonArray((it as? Collection)?.size ?: 10)) { acc, elem ->
                acc.add(serialize(elem))
                acc
            }
        )
        addProperty("type", type.serializeValue)
    }


/*private fun toRef(s: Screen, converters: Converters): Value<*> = s.toValue(converters)



private object URLConverter : Converter<URL, StringWrapper> {

    override fun from(v: StringWrapper, converters: Converters): URL? = URL(v.value)

    override fun to(t: URL, converters: Converters): StringWrapper =
        wrap(t.toExternalForm())

}*/
