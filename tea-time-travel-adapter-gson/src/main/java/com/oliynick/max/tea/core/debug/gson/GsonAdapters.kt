@file:Suppress("UNCHECKED_CAST")

package com.oliynick.max.tea.core.debug.gson

import com.google.gson.*
import protocol.*
import java.lang.reflect.Type
import java.util.*

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

internal object ServerMessageAdapter : JsonSerializer<ServerMessage>,
    JsonDeserializer<ServerMessage> {

    override fun serialize(
        src: ServerMessage,
        typeOfSrc: Type?,
        context: JsonSerializationContext
    ): JsonElement {

        val tree: JsonObject = when (src) {
            is NotifyComponentSnapshot -> src.toJsonElement()
            is NotifyComponentAttached -> src.toJsonElement()
            is ActionApplied -> src.toJsonElement(context)
        }

        tree.addProperty("@type", src::class.java.name)

        return tree
    }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext
    ): ServerMessage = json.asJsonObject.let { obj ->

        when (obj["@type"].asString) {
            NotifyComponentSnapshot::class.java.name -> json.asNotifyComponentSnapshot()
            NotifyComponentAttached::class.java.name -> json.asNotifyComponentAttached()
            ActionApplied::class.java.name -> json.asActionApplied(context)
            else -> error("unknown server message type, json\n\n$json\n")
        }
    }

    private fun NotifyComponentSnapshot.toJsonElement() =
        JsonObject {
            add("message", message)
            add("oldState", oldState)
            add("newState", newState)
        }

    private fun JsonElement.asNotifyComponentSnapshot() =
        NotifyComponentSnapshot(
            asJsonObject["message"],
            asJsonObject["oldState"],
            asJsonObject["newState"]
        )

    private fun NotifyComponentAttached.toJsonElement(): JsonObject = JsonObject {
        add("state", state)
    }

    private fun JsonElement.asNotifyComponentAttached() = NotifyComponentAttached(asJsonObject["state"])

    private fun ActionApplied.toJsonElement(
        context: JsonSerializationContext
    ): JsonObject = JsonObject {
        add("id", context.serialize(id))
    }

    private fun JsonElement.asActionApplied(context: JsonDeserializationContext) =
        ActionApplied(context.deserialize(asJsonObject["id"], UUID::class.java))

}

/*
internal object ClientMessageAdapter : JsonSerializer<ClientMessage>,
    JsonDeserializer<ClientMessage> {

    override fun serialize(
        src: ClientMessage,
        typeOfSrc: Type?,
        context: JsonSerializationContext
    ): JsonElement = JsonObject {
        addProperty("@type", src::class.java.serializeName)

        add(
            "@value",
            when (src) {
                is ApplyMessage -> src.toJsonElement()
                is ApplyState -> src.toJsonElement()
            }
        )
    }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext
    ): ClientMessage = json.asJsonObject.let { obj ->

        val value = obj["@type"]

        when (obj["@type"].asString) {
            ApplyMessage::class.java.serializeName -> value.asApplyMessage()
            ApplyState::class.java.serializeName -> value.asApplyState()
            else -> error("unknown client message type, json\n\n$json\n")
        }
    }

    private fun ApplyMessage.toJsonElement() = message

    private fun ApplyState.toJsonElement() = state

    private fun JsonElement.asApplyMessage() = ApplyMessage(this)

    private fun JsonElement.asApplyState() = ApplyState(this)

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
        context.fromTypedJson<JsonElement>(json)
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
    ): ApplyMessage = ApplyMessage(json)

}*/
