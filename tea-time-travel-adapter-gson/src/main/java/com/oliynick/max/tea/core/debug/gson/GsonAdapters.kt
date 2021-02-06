/*
 * Copyright (C) 2021. Maksym Oliinyk.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
    ): JsonElement = JsonPrimitive(src.value)

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext
    ): ComponentId =
        ComponentId(json.asString)
}

internal object ServerMessageAdapter : JsonSerializer<GsonServerMessage>,
    JsonDeserializer<GsonServerMessage> {

    override fun serialize(
        src: GsonServerMessage,
        typeOfSrc: Type?,
        context: JsonSerializationContext
    ): JsonElement {

        val tree: JsonObject = when (src) {
            is GsonNotifyComponentSnapshot -> src.toJsonElement()
            is GsonNotifyComponentAttached -> src.toJsonElement()
        }

        tree.addProperty("@type", src::class.java.name)

        return tree
    }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext
    ): GsonServerMessage = json.asJsonObject.let { obj ->

        when (obj["@type"].asString) {
            NotifyComponentSnapshot::class.java.name -> json.asNotifyComponentSnapshot()
            NotifyComponentAttached::class.java.name -> json.asNotifyComponentAttached()

            else -> error("unknown server message type, json\n\n$json\n")
        }
    }

    private fun GsonNotifyComponentSnapshot.toJsonElement() =
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

    private fun GsonNotifyComponentAttached.toJsonElement(): JsonObject = JsonObject {
        add("state", state)
    }

    private fun JsonElement.asNotifyComponentAttached() =
        NotifyComponentAttached(asJsonObject["state"])

}

internal object ClientMessageAdapter : JsonSerializer<GsonClientMessage>,
    JsonDeserializer<GsonClientMessage> {

    override fun serialize(
        src: GsonClientMessage,
        typeOfSrc: Type?,
        context: JsonSerializationContext
    ): JsonElement {

        val tree: JsonObject = when(src) {
            is GsonApplyMessage -> src.toJsonElement()
            is GsonApplyState -> src.toJsonElement()
        }

        tree.addProperty("@type", src::class.java.name)

        return tree
    }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext
    ): GsonClientMessage = json.asJsonObject.let { obj ->

        when (obj["@type"].asString) {
            ApplyMessage::class.java.name -> json.asApplyMessage()
            ApplyState::class.java.name -> json.asApplyState()
            else -> error("unknown server message type, json\n\n$json\n")
        }
    }

    private fun GsonApplyMessage.toJsonElement() = JsonObject {
        add("message", message)
    }

    private fun GsonApplyState.toJsonElement() = JsonObject {
        add("state", state)
    }

    private fun JsonElement.asApplyMessage() =
        ApplyMessage(asJsonObject["message"])

    private fun JsonElement.asApplyState() = ApplyState(asJsonObject["state"])

}

private inline fun JsonObject(
    builder: JsonObject.() -> Unit
): JsonObject = JsonObject().apply(builder)