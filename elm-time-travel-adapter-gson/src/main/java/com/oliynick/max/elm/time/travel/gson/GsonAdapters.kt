@file:Suppress("UNCHECKED_CAST")

package com.oliynick.max.elm.time.travel.gson

import com.google.gson.*
import protocol.*
import java.lang.reflect.Type
import java.util.*

object ServerMessageAdapter : JsonSerializer<ServerMessage>, JsonDeserializer<ServerMessage> {

    override fun serialize(
        src: ServerMessage,
        typeOfSrc: Type?,
        context: JsonSerializationContext
    ): JsonElement = context.serialize(src)

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext
    ): ServerMessage = context.deserialize(json, json.asJsonObject["type"].asString.clazz())
}

object ClientMessageAdapter : JsonSerializer<ClientMessage>, JsonDeserializer<ClientMessage> {

    override fun serialize(
        src: ClientMessage,
        typeOfSrc: Type?,
        context: JsonSerializationContext
    ): JsonElement = context.serialize(src)

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext
    ): ClientMessage = context.deserialize(json, json.asJsonObject["type"].asString.clazz())
}

object NotifyComponentSnapshotAdapter : JsonSerializer<NotifyComponentSnapshot>,
    JsonDeserializer<NotifyComponentSnapshot> {

    override fun serialize(
        src: NotifyComponentSnapshot,
        typeOfSrc: Type?,
        context: JsonSerializationContext
    ): JsonElement = src.typedJsonObject {
        add("message", context.serialize(src.message))
        add("oldState", context.serialize(src.oldState))
        add("newState", context.serialize(src.newState))
    }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext
    ): NotifyComponentSnapshot {
        val jsonObject = json.asJsonObject

        fun deserialize(propertyName: String) =
            context.deserialize<Json>(jsonObject[propertyName], Any::class.java)

        return NotifyComponentSnapshot(
            deserialize("message"),
            deserialize("oldState"),
            deserialize("newState")
        )
    }
}

object ApplyStateAdapter : JsonSerializer<ApplyState>, JsonDeserializer<ApplyState> {

    override fun serialize(
        src: ApplyState,
        typeOfSrc: Type?,
        context: JsonSerializationContext
    ): JsonElement = src.typedJsonObject { add("state", context.serialize(src.stateValue)) }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext
    ): ApplyState = ApplyState(context.deserialize(json.asJsonObject["state"], Any::class.java))
}

object ApplyMessageAdapter : JsonSerializer<ApplyMessage>, JsonDeserializer<ApplyMessage> {

    override fun serialize(
        src: ApplyMessage,
        typeOfSrc: Type?,
        context: JsonSerializationContext
    ): JsonElement = src.typedJsonObject { add("message", context.serialize(src.messageValue)) }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext
    ): ApplyMessage =
        ApplyMessage(context.deserialize(json.asJsonObject["message"], Any::class.java))
}

object NotifyComponentAttachedAdapter : JsonSerializer<NotifyComponentAttached>,
    JsonDeserializer<NotifyComponentAttached> {

    override fun serialize(
        src: NotifyComponentAttached,
        typeOfSrc: Type?,
        context: JsonSerializationContext
    ): JsonElement = src.typedJsonObject { add("state", context.serialize(src.state)) }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext
    ): NotifyComponentAttached = NotifyComponentAttached(
        context.deserialize(
            json.asJsonObject["state"],
            Any::class.java
        )
    )
}

object ActionAppliedAdapter : JsonSerializer<ActionApplied>, JsonDeserializer<ActionApplied> {

    override fun serialize(
        src: ActionApplied,
        typeOfSrc: Type?,
        context: JsonSerializationContext
    ): JsonElement = src.typedJsonObject { add("id", context.serialize(src.id)) }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext
    ): ActionApplied = ActionApplied(context.deserialize(json.asJsonObject["id"], UUID::class.java))
}

object UUIDAdapter : JsonSerializer<UUID>, JsonDeserializer<UUID> {

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

object ComponentIdAdapter : JsonSerializer<ComponentId>, JsonDeserializer<ComponentId> {

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

private inline fun Any.typedJsonObject(
    typePropertyName: String = "type",
    block: JsonObject.() -> Unit
): JsonObject {
    return JsonObject()
        .apply { addProperty(typePropertyName, this@typedJsonObject::class.java.serializeName) }
        .apply(block)
}

private fun String.clazz() = Class.forName(this)

inline val Class<*>.serializeName: String get() = name
