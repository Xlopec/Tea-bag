@file:Suppress("UNCHECKED_CAST", "FunctionName")

package com.oliynick.max.tea.core.debug.gson

import com.google.gson.*
import com.oliynick.max.tea.core.debug.protocol.*
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
    ): ComponentId =
        ComponentId(json.asString)
}

internal object ServerMessageAdapter : JsonSerializer<ServerMessage<JsonElement>>,
    JsonDeserializer<ServerMessage<JsonElement>> {

    override fun serialize(
        src: ServerMessage<JsonElement>,
        typeOfSrc: Type?,
        context: JsonSerializationContext
    ): JsonElement {

        val tree: JsonObject = when (src) {
            is NotifyComponentSnapshot<JsonElement> -> src.toJsonElement()
            is NotifyComponentAttached<JsonElement> -> src.toJsonElement()
        }

        tree.addProperty("@type", src::class.java.name)

        return tree
    }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext
    ): ServerMessage<JsonElement> = json.asJsonObject.let { obj ->

        when (obj["@type"].asString) {
            NotifyComponentSnapshot::class.java.name -> json.asNotifyComponentSnapshot()
            NotifyComponentAttached::class.java.name -> json.asNotifyComponentAttached()

            else -> error("unknown server message type, json\n\n$json\n")
        }
    }

    private fun NotifyComponentSnapshot<JsonElement>.toJsonElement() =
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

    private fun NotifyComponentAttached<JsonElement>.toJsonElement(): JsonObject = JsonObject {
        add("state", state)
    }

    private fun JsonElement.asNotifyComponentAttached() =
        NotifyComponentAttached(asJsonObject["state"])

}

internal object ClientMessageAdapter : JsonSerializer<ClientMessage<JsonElement>>,
    JsonDeserializer<ClientMessage<JsonElement>> {

    override fun serialize(
        src: ClientMessage<JsonElement>,
        typeOfSrc: Type?,
        context: JsonSerializationContext
    ): JsonElement {

        val tree: JsonObject = when(src) {
            is ApplyMessage<JsonElement> -> src.toJsonElement()
            is ApplyState<JsonElement> -> src.toJsonElement()
        }

        tree.addProperty("@type", src::class.java.name)

        return tree
    }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext
    ): ClientMessage<JsonElement> = json.asJsonObject.let { obj ->

        when (obj["@type"].asString) {
            ApplyMessage::class.java.name -> json.asApplyMessage()
            ApplyState::class.java.name -> json.asApplyState()
            else -> error("unknown server message type, json\n\n$json\n")
        }
    }

    private fun ApplyMessage<JsonElement>.toJsonElement() = JsonObject {
        add("message", message)
    }

    private fun ApplyState<JsonElement>.toJsonElement() = JsonObject {
        add("state", state)
    }

    private fun JsonElement.asApplyMessage() =
        ApplyMessage(asJsonObject["message"])

    private fun JsonElement.asApplyState() = ApplyState(asJsonObject["state"])

}

private inline fun JsonObject(
    builder: JsonObject.() -> Unit
): JsonObject = JsonObject().apply(builder)