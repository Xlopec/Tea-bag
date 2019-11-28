@file:Suppress("UNCHECKED_CAST")

package com.oliynick.max.elm.time.travel.gson

import com.google.gson.*
import protocol.*
import java.lang.reflect.Type
import java.util.*

object CollectionAdapter : JsonSerializer<CollectionWrapper>, JsonDeserializer<CollectionWrapper> {

    override fun serialize(
        src: CollectionWrapper,
        typeOfSrc: Type?,
        context: JsonSerializationContext
    ): JsonElement {
        return JsonArray(src.value.size).also { arr ->
            src.value.forEach { elem -> arr.add(context.serialize(elem)) }
        }
    }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext
    ): CollectionWrapper = CollectionWrapper(json.asJsonArray.map { jsonElem ->
        context.deserialize(
            jsonElem,
            Value::class.java
        )
    })
}

object MapAdapter : JsonSerializer<MapWrapper>, JsonDeserializer<MapWrapper> {

    override fun serialize(
        src: MapWrapper,
        typeOfSrc: Type?,
        context: JsonSerializationContext
    ): JsonElement {
        return JsonArray(src.value.size).also { arr ->
            src.value.forEach { entry ->
                arr.add(JsonObject().apply {
                    add("key", context.serialize(entry.key))
                    add("value", context.serialize(entry.value))
                })
            }
        }
    }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext
    ): MapWrapper = MapWrapper(json.asJsonArray
        .asSequence()
        .map { it.asJsonObject }
        .map { jsonObject ->

            context.deserialize<Value<*>>(
                jsonObject["key"],
                Value::class.java
            ) to context.deserialize<Value<*>>(
                jsonObject["value"],
                Value::class.java
            )
        }.toMap()
    )
}

object IntAdapter : JsonSerializer<IntWrapper>, JsonDeserializer<IntWrapper> {
    override fun serialize(src: IntWrapper, typeOfSrc: Type?, context: JsonSerializationContext) =
        src.toJson()

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext
    ) = IntWrapper(json.asJsonObject["value"].asInt)
}

object LongAdapter : JsonSerializer<LongWrapper>, JsonDeserializer<LongWrapper> {
    override fun serialize(src: LongWrapper, typeOfSrc: Type?, context: JsonSerializationContext) =
        src.toJson()

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext
    ) = LongWrapper(json.asJsonObject["value"].asLong)
}

object ByteAdapter : JsonSerializer<ByteWrapper>, JsonDeserializer<ByteWrapper> {
    override fun serialize(src: ByteWrapper, typeOfSrc: Type?, context: JsonSerializationContext) =
        src.toJson()

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext
    ) = ByteWrapper(json.asJsonObject["value"].asByte)
}

object ShortAdapter : JsonSerializer<ShortWrapper>, JsonDeserializer<ShortWrapper> {
    override fun serialize(src: ShortWrapper, typeOfSrc: Type?, context: JsonSerializationContext) =
        src.toJson()

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext
    ) = ShortWrapper(json.asJsonObject["value"].asShort)
}

object CharAdapter : JsonSerializer<CharWrapper>, JsonDeserializer<CharWrapper> {
    override fun serialize(src: CharWrapper, typeOfSrc: Type?, context: JsonSerializationContext) =
        src.toJson()

    @Suppress("DEPRECATION")
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext
    ) = CharWrapper(json.asJsonObject["value"].asCharacter)
}

object BooleanAdapter : JsonSerializer<BooleanWrapper>, JsonDeserializer<BooleanWrapper> {
    override fun serialize(
        src: BooleanWrapper,
        typeOfSrc: Type?,
        context: JsonSerializationContext
    ) = src.toJson()

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext
    ) = BooleanWrapper.of(json.asJsonObject["value"].asBoolean)
}

object DoubleAdapter : JsonSerializer<DoubleWrapper>, JsonDeserializer<DoubleWrapper> {
    override fun serialize(
        src: DoubleWrapper,
        typeOfSrc: Type?,
        context: JsonSerializationContext
    ) = src.toJson()

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext
    ) = DoubleWrapper(json.asJsonObject["value"].asDouble)
}

object FloatAdapter : JsonSerializer<FloatWrapper>, JsonDeserializer<FloatWrapper> {
    override fun serialize(src: FloatWrapper, typeOfSrc: Type?, context: JsonSerializationContext) =
        src.toJson()

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext
    ) = FloatWrapper(json.asJsonObject["value"].asFloat)
}

object StringAdapter : JsonSerializer<StringWrapper>, JsonDeserializer<StringWrapper> {
    override fun serialize(
        src: StringWrapper,
        typeOfSrc: Type?,
        context: JsonSerializationContext
    ) = src.toJson()

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext
    ) = StringWrapper(json.asJsonObject["value"].asString)
}

object NullAdapter : JsonSerializer<Null>, JsonDeserializer<Null> {
    override fun serialize(
        src: Null,
        typeOfSrc: Type?,
        context: JsonSerializationContext
    ): JsonElement = JsonObject().apply {
        addProperty("wrapper_type", src.javaClass.name)
        addProperty("underlying_type", src.type.value)
        add("value", null)
    }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext
    ): Null {
        return Null(RemoteType(json.asJsonObject["underlying_type"].asString.clazz()))
    }
}

object ValueDeserializer : JsonDeserializer<Value<*>> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext
    ): Value<*> {
        val jsonObject = json.asJsonObject
        val wrapperType = jsonObject["wrapper_type"].asString.wrapperType()

        return context.deserialize(
            jsonObject,
            wrapperType
        )
    }
}

object RefAdapter : JsonSerializer<Ref>, JsonDeserializer<Ref> {

    override fun serialize(
        src: Ref,
        typeOfSrc: Type?,
        context: JsonSerializationContext
    ): JsonElement = JsonObject()
        .apply {
            addProperty("underlying_type", src.type.value)
            addProperty("wrapper_type", src::class.java.serializeName)
            add("properties", src.properties.toJsonProperties(context::serialize))
        }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext
    ): Ref = json.asJsonObject.run {
        Ref(
            RemoteType(this["underlying_type"].asString),
            this["properties"].asJsonArray.fromJsonProperties(context::deserialize)
        )
    }

}

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

object NotifyComponentSnapshotAdapter : JsonSerializer<NotifyComponentSnapshot<*, *>>,
    JsonDeserializer<NotifyComponentSnapshot<*, *>> {

    override fun serialize(
        src: NotifyComponentSnapshot<*, *>,
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
    ): NotifyComponentSnapshot<*, *> {
        val jsonObject = json.asJsonObject

        fun deserialize(propertyName: String) =
            context.deserialize<Value<*>>(jsonObject[propertyName], Value::class.java)

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
    ): ApplyState = ApplyState(context.deserialize(json.asJsonObject["state"], Value::class.java))
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
        ApplyMessage(context.deserialize(json.asJsonObject["message"], Value::class.java))
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
            Value::class.java
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


private fun String.wrapperType() = Class.forName(this) as Class<out Value<*>>

private fun String.clazz() = Class.forName(this)

private fun PrimitiveWrapper<*>.toJson(): JsonObject {
    val jsonPrimitive = when (this) {
        is IntWrapper -> JsonPrimitive(value)
        is ByteWrapper -> JsonPrimitive(value)
        is ShortWrapper -> JsonPrimitive(value)
        is CharWrapper -> JsonPrimitive(value)
        is LongWrapper -> JsonPrimitive(value)
        is DoubleWrapper -> JsonPrimitive(value)
        is FloatWrapper -> JsonPrimitive(value)
        is StringWrapper -> JsonPrimitive(value)
        is BooleanWrapper -> JsonPrimitive(value)
    }

    return JsonObject().apply {
        addProperty("wrapper_type", this@toJson.javaClass.name)
        addProperty("underlying_type", this@toJson.type.value)
        add("value", jsonPrimitive)
    }
}

private inline fun Collection<Property<*>>.toJsonProperties(serializer: (Any?) -> JsonElement): JsonArray =
    fold(JsonArray(size)) { arr, property ->
        arr.add(property.toJsonProperty(serializer))
        arr
    }

private inline fun Property<*>.toJsonProperty(serializer: (Any?) -> JsonElement): JsonObject =
    JsonObject().apply {
        addProperty("property_name", name)
        addProperty("wrapper_type", v::class.java.serializeName)
        addProperty("underlying_type", type.value)
        add("property", serializer(v))
    }

private inline fun JsonArray.fromJsonProperties(deserializer: (JsonElement, Type) -> Value<*>): Set<Property<*>> =
    map { e -> e.asJsonObject }
        .map { o -> o.fromJsonProperty(deserializer) }
        .toSet()


private inline fun JsonObject.fromJsonProperty(deserializer: (JsonElement, Type) -> Value<*>): Property<*> =
    Property(
        RemoteType(this["underlying_type"].asString),
        this["property_name"].asString,
        deserializer(this["property"], this["wrapper_type"].asString.wrapperType())
    )