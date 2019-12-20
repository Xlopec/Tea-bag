@file:Suppress("UNCHECKED_CAST")

package com.oliynick.max.elm.time.travel.gson

import com.google.gson.*
import protocol.*
import java.lang.reflect.Type
import java.util.*
import kotlin.collections.ArrayList

internal object ServerMessageAdapter : JsonSerializer<ServerMessage>, JsonDeserializer<ServerMessage> {

    override fun serialize(
        src: ServerMessage,
        typeOfSrc: Type?,
        context: JsonSerializationContext
    ): JsonElement = context.toTypedJson(src)

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext
    ): ServerMessage = context.fromTypedJson(json)
}

internal object ClientMessageAdapter : JsonSerializer<ClientMessage>, JsonDeserializer<ClientMessage> {

    override fun serialize(
        src: ClientMessage,
        typeOfSrc: Type?,
        context: JsonSerializationContext
    ): JsonElement = context.toTypedJson(src)

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext
    ): ClientMessage = context.fromTypedJson(json)
}

internal object NotifyComponentSnapshotAdapter : JsonSerializer<NotifyComponentSnapshot>,
                                                 JsonDeserializer<NotifyComponentSnapshot> {

    override fun serialize(
        src: NotifyComponentSnapshot,
        typeOfSrc: Type?,
        context: JsonSerializationContext
    ): JsonElement = JsonObject {
        add("message", context.toTypedJson(src.message))
        add("oldState", context.toTypedJson(src.oldState))
        add("newState", context.toTypedJson(src.newState))
    }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext
    ): NotifyComponentSnapshot {
        val jsonObject = json.asJsonObject

        fun deserialize(propertyName: String) =
            context.fromTypedJson<Any>(jsonObject[propertyName])

        return NotifyComponentSnapshot(
            deserialize("message"),
            deserialize("oldState"),
            deserialize("newState")
        )
    }
}

internal object ApplyStateAdapter : JsonSerializer<ApplyState>, JsonDeserializer<ApplyState> {

    override fun serialize(
        src: ApplyState,
        typeOfSrc: Type?,
        context: JsonSerializationContext
    ): JsonElement = context.toTypedJson(src.state)

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext
    ): ApplyState =
        context.fromTypedJson<Any>(json)
            .let(::ApplyState)
}

internal object ApplyMessageAdapter : JsonSerializer<ApplyMessage>, JsonDeserializer<ApplyMessage> {

    override fun serialize(
        src: ApplyMessage,
        typeOfSrc: Type?,
        context: JsonSerializationContext
    ): JsonElement = context.toTypedJson(src.message)

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext
    ): ApplyMessage =
        context.fromTypedJson<Any>(json)
            .let(::ApplyMessage)

}

internal object NotifyComponentAttachedAdapter : JsonSerializer<NotifyComponentAttached>,
                                                 JsonDeserializer<NotifyComponentAttached> {

    override fun serialize(
        src: NotifyComponentAttached,
        typeOfSrc: Type?,
        context: JsonSerializationContext
    ): JsonElement = context.toTypedJson(src.state)

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext
    ): NotifyComponentAttached =
        context.fromTypedJson<Any>(json)
            .let(::NotifyComponentAttached)
}

internal object ActionAppliedAdapter : JsonSerializer<ActionApplied>, JsonDeserializer<ActionApplied> {

    override fun serialize(
        src: ActionApplied,
        typeOfSrc: Type?,
        context: JsonSerializationContext
    ): JsonElement = context.toTypedJson(src.id)

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext
    ): ActionApplied = context.fromTypedJson<UUID>(json)
        .let(::ActionApplied)
}

internal object UUIDAdapter : JsonSerializer<UUID>, JsonDeserializer<UUID> {

    override fun serialize(
        src: UUID,
        typeOfSrc: Type?,
        context: JsonSerializationContext
    ): JsonElement = JsonPrimitive(src.toString())

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext
    ): UUID = UUID.fromString(json.asString)
}

internal object ComponentIdAdapter : JsonSerializer<ComponentId>, JsonDeserializer<ComponentId> {

    override fun serialize(
        src: ComponentId,
        typeOfSrc: Type?,
        context: JsonSerializationContext
    ): JsonElement = JsonPrimitive(src.id)

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext
    ): ComponentId = ComponentId(json.asString)
}

internal object ListSerializer : JsonSerializer<List<*>>, JsonDeserializer<List<*>> {

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext
    ): List<*> = context.fromTypedJsonArray<Any?, ArrayList<Any?>>(json, ArrayList(json.asJsonArray.size()))

    override fun serialize(
        src: List<*>,
        typeOfSrc: Type?,
        context: JsonSerializationContext
    ): JsonElement =
        JsonArray(src.map { elem -> context.toTypedJson(elem) })

}

@Suppress("FunctionName")
private inline fun JsonObject(
    builder: JsonObject.() -> Unit
): JsonObject = JsonObject().apply(builder)

@Suppress("FunctionName")
private inline fun JsonArray(
    capacity: Int = 10,
    builder: JsonArray.() -> Unit
): JsonArray = JsonArray(capacity).apply(builder)

@Suppress("FunctionName")
private fun JsonArray(
    it: Iterable<JsonElement>
): JsonArray = JsonArray((it as? Collection)?.size ?: 10) { it.forEach(::add) }

private fun JsonSerializationContext.toTypedJson(any: Any?) =
    JsonObject()
        .apply {
            addProperty("type", if (any == null) Any::class.java.serializeName else any::class.java.serializeName)
            add("value", serialize(any))
        }

private fun <T> JsonDeserializationContext.fromTypedJson(json: JsonElement): T =
    json.asJsonObject.let { obj ->
        deserialize(obj["value"], obj["type"].asString.clazz())
    }

private fun <T, C : MutableCollection<T>> JsonDeserializationContext.fromTypedJsonArray(
    json: JsonElement,
    into: C
): C =
    json.asJsonArray
        .asSequence()
        .map { elem -> fromTypedJson<T>(elem) }
        .toCollection(into)

private fun String.clazz() = Class.forName(this)

inline val Class<*>.serializeName: String get() = name
