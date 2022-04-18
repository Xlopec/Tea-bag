/*
 * MIT License
 *
 * Copyright (c) 2022. Maksym Oliinyk.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

@file:Suppress("UNCHECKED_CAST", "FunctionName")

package io.github.xlopec.tea.core.debug.gson

import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import io.github.xlopec.tea.core.debug.protocol.ApplyMessage
import io.github.xlopec.tea.core.debug.protocol.ApplyState
import io.github.xlopec.tea.core.debug.protocol.ComponentId
import io.github.xlopec.tea.core.debug.protocol.NotifyComponentAttached
import io.github.xlopec.tea.core.debug.protocol.NotifyComponentSnapshot
import io.github.xlopec.tea.data.UUID
import io.github.xlopec.tea.data.toHumanReadable
import java.lang.reflect.Type

internal object UUIDAdapter : JsonSerializer<UUID>, JsonDeserializer<UUID> {

    override fun serialize(
        src: UUID,
        typeOfSrc: Type?,
        context: JsonSerializationContext
    ): JsonElement = JsonPrimitive(src.toHumanReadable())

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
            add("commands", commands.toJsonArray())
        }

    private fun JsonElement.asNotifyComponentSnapshot() =
        NotifyComponentSnapshot(
            asJsonObject["message"],
            asJsonObject["oldState"],
            asJsonObject["newState"],
            asJsonObject["commands"].asJsonArray.toJsonElementSet(),
        )

    private fun GsonNotifyComponentAttached.toJsonElement(): JsonObject = JsonObject {
        add("state", state)
        add("commands", commands.toJsonArray())
    }

    private fun JsonElement.asNotifyComponentAttached() =
        NotifyComponentAttached(asJsonObject["state"], asJsonObject["commands"].asJsonArray.toJsonElementSet())

}

internal object ClientMessageAdapter : JsonSerializer<GsonClientMessage>,
    JsonDeserializer<GsonClientMessage> {

    override fun serialize(
        src: GsonClientMessage,
        typeOfSrc: Type?,
        context: JsonSerializationContext
    ): JsonElement {

        val tree: JsonObject = when (src) {
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

private fun Set<JsonElement>.toJsonArray() = JsonArray(size).apply { this@toJsonArray.forEach(::add) }

private fun JsonArray.toJsonElementSet(): Set<JsonElement> = mapTo(HashSet(size())) { it }
