package com.oliynick.max.elm.time.travel.protocol

import com.google.gson.*
import com.google.gson.reflect.*
import java.lang.reflect.*
import java.util.*
import kotlin.reflect.*

val gson: Gson = GsonBuilder().setPrettyPrinting().serializeNulls()
        .registerTypeAdapter(UUIDParser.type, UUIDParser)
        .registerTypeAdapter(ActionParser.type, ActionParser)
        .create()

private interface JsonTypeAdapter<T> : JsonDeserializer<T>, JsonSerializer<T> {
    val type: Class<out T>
}

private object UUIDParser : JsonTypeAdapter<UUID> {
    override val type: Class<out UUID> = UUID::class.java

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): UUID {
        return UUID.fromString(json.asString)
    }

    override fun serialize(src: UUID, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return JsonPrimitive(src.toString())
    }

}

private object ActionParser : JsonTypeAdapter<Action> {

    override val type: Class<out Action> = Action::class.java

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Action {

        val obj = json.asJsonObject

        return when (val type = obj["type"].asString) {

            ApplyCommands::class.typeKey -> context.parseCommands(obj)

            else -> notifyUnknownActionType(type)
        }
    }

    override fun serialize(src: Action, typeOfSrc: Type?, context: JsonSerializationContext): JsonElement {
        return JsonObject().also { obj ->
            obj.addProperty("type", src.typeKey)
            obj.add("payload", context.toJsonElement(src))
        }
    }

}

private fun JsonSerializationContext.toJsonElement(src: Action): JsonElement = when (src) {
    is ApplyCommands -> serialize(src.commands)
}

private val commandsTypeToken = object : TypeToken<List<@JvmSuppressWildcards Any>>() {}

private fun JsonDeserializationContext.parseCommands(obj: JsonObject): ApplyCommands {
    return ApplyCommands(deserialize(obj["payload"], commandsTypeToken.type))
}

private val Action.typeKey: String
    inline get() = this::class.typeKey

private val KClass<out Action>.typeKey: String
    inline get() = requireNotNull(java.canonicalName) { "What a terrible failure $this" }

private fun notifyUnknownActionType(type: String): Nothing {
    throw JsonParseException("unknown type $type")
}