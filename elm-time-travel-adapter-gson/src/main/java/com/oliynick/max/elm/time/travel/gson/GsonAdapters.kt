package com.oliynick.max.elm.time.travel.gson

import com.google.gson.*
import protocol.*
import java.lang.reflect.Type
import java.util.*

// todo replace direct constructor invocations with `wrap` method

object IterableAdapter : JsonSerializer<IterableWrapper>, JsonDeserializer<IterableWrapper> {

    override fun serialize(
        src: IterableWrapper,
        typeOfSrc: Type?,
        context: JsonSerializationContext
    ): JsonElement {
        return JsonArray((src.value as? Collection<*>)?.size ?: 10).also { arr ->
            src.value.forEach { elem -> arr.add(context.serialize(elem)) }
        }
    }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext
    ): IterableWrapper {
        return IterableWrapper(json.asJsonArray.map { jsonElem ->
            context.deserialize(
                jsonElem,
                Value::class.java
            )
        })
    }
}

object IntAdapter : JsonSerializer<IntWrapper>, JsonDeserializer<IntWrapper> {
    override fun serialize(src: IntWrapper, typeOfSrc: Type?, context: JsonSerializationContext) =
        JsonPrimitive(src.value)

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext
    ) = IntWrapper(json.asJsonObject["value"].asInt)
}

object LongAdapter : JsonSerializer<LongWrapper>, JsonDeserializer<LongWrapper> {
    override fun serialize(src: LongWrapper, typeOfSrc: Type?, context: JsonSerializationContext) =
        JsonPrimitive(src.value)

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext
    ) = LongWrapper(json.asJsonObject["value"].asLong)
}

object ByteAdapter : JsonSerializer<ByteWrapper>, JsonDeserializer<ByteWrapper> {
    override fun serialize(src: ByteWrapper, typeOfSrc: Type?, context: JsonSerializationContext) =
        JsonPrimitive(src.value)

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext
    ) = ByteWrapper(json.asJsonObject["value"].asByte)
}

object ShortAdapter : JsonSerializer<ShortWrapper>, JsonDeserializer<ShortWrapper> {
    override fun serialize(src: ShortWrapper, typeOfSrc: Type?, context: JsonSerializationContext) =
        JsonPrimitive(src.value)

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext
    ) = ShortWrapper(json.asJsonObject["value"].asShort)
}

object CharAdapter : JsonSerializer<CharWrapper>, JsonDeserializer<CharWrapper> {
    override fun serialize(src: CharWrapper, typeOfSrc: Type?, context: JsonSerializationContext) =
        JsonPrimitive(src.value)

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
    ) = JsonPrimitive(src.value)

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
    ) = JsonPrimitive(src.value)

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext
    ) = DoubleWrapper(json.asJsonObject["value"].asDouble)
}

object FloatAdapter : JsonSerializer<FloatWrapper>, JsonDeserializer<FloatWrapper> {
    override fun serialize(src: FloatWrapper, typeOfSrc: Type?, context: JsonSerializationContext) =
        JsonPrimitive(src.value)

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
    ) = toJson(src)//JsonPrimitive(src.value)

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

private fun toJson(p: PrimitiveWrapper<*>): JsonObject {
    val v = when(p) {
        is IntWrapper -> JsonPrimitive(p.value)
        is ByteWrapper -> JsonPrimitive(p.value)
        is ShortWrapper -> JsonPrimitive(p.value)
        is CharWrapper -> JsonPrimitive(p.value)
        is LongWrapper -> JsonPrimitive(p.value)
        is DoubleWrapper -> JsonPrimitive(p.value)
        is FloatWrapper -> JsonPrimitive(p.value)
        is StringWrapper -> JsonPrimitive(p.value)
        is BooleanWrapper -> JsonPrimitive(p.value)
    }
    return JsonObject().apply {
        addProperty("wrapper_type", p.javaClass.name)
        addProperty("underlying_type", p.type.value)
        add("value", v)
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
    ): JsonElement {
        val obj = JsonObject()
            .also { obj ->
                obj.addProperty("underlying_type", src.type.value)
                obj.addProperty("wrapper_type", src::class.java.serializeName)
            }

        val arr = JsonArray(src.properties.size)

        for (p in src.properties) {
            val o = JsonObject().also {
                it.addProperty("property_name", p.name)
                it.addProperty("wrapper_type", p.v::class.java.serializeName)
                it.addProperty("underlying_type", p.type.value)
                it.add("property", context.serialize(p.v))
            }

            arr.add(o)
        }

        obj.add("properties", arr)

        return obj
    }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext
    ): Ref {
        // todo refactor
        val jsonObj = json.asJsonObject
        val wrapperType = Class.forName(jsonObj["wrapper_type"].asString) as Class<out Value<*>>
        val underlying = jsonObj["underlying_type"].asString

        return Ref(RemoteType(underlying),
            jsonObj[wrapperType.payloadFieldName].asJsonArray
                .map { e -> e.asJsonObject }
                .map { o ->

                    val cll =
                        Class.forName(o["wrapper_type"].asString) as Class<out Value<*>>
                    val nested = o["property"]

                    val value =/* if (nested.isJsonNull) {
                        context.deserialize<Value<Any>>(o, cll)
                    } else {
                        */context.deserialize<Value<Any>>(nested, cll)
                    //}

                    Property(
                        RemoteType(o["underlying_type"].asString),
                        o["property_name"].asString,
                        value
                    )
                }.toSet()
        )
    }

}

object ServerMessageAdapter : JsonSerializer<ServerMessage>, JsonDeserializer<ServerMessage> {

    override fun serialize(
        src: ServerMessage,
        typeOfSrc: Type?,
        context: JsonSerializationContext
    ): JsonElement {
        return context.serialize(src)
    }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext
    ): ServerMessage {
        return context.deserialize(json, json.asJsonObject["type"].asString.clazz())
    }
}

object ClientMessageAdapter : JsonSerializer<ClientMessage>, JsonDeserializer<ClientMessage> {

    override fun serialize(
        src: ClientMessage,
        typeOfSrc: Type?,
        context: JsonSerializationContext
    ): JsonElement {
        return context.serialize(src)
    }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext
    ): ClientMessage {
        return context.deserialize(json, json.asJsonObject["type"].asString.clazz())
    }
}

object NotifyComponentSnapshotAdapter : JsonSerializer<NotifyComponentSnapshot<*, *>>,
    JsonDeserializer<NotifyComponentSnapshot<*, *>> {

    override fun serialize(
        src: NotifyComponentSnapshot<*, *>,
        typeOfSrc: Type?,
        context: JsonSerializationContext
    ): JsonElement {
        return src.typedJsonObject {
            add("message", context.serialize(src.message))
            add("oldState", context.serialize(src.oldState))
            add("newState", context.serialize(src.newState))
        }
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
    ): JsonElement {
        return src.typedJsonObject { add("state", context.serialize(src.stateValue)) }
    }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext
    ): ApplyState {
        return ApplyState(context.deserialize(json.asJsonObject["state"], Value::class.java))
    }
}

object ApplyMessageAdapter : JsonSerializer<ApplyMessage>, JsonDeserializer<ApplyMessage> {

    override fun serialize(
        src: ApplyMessage,
        typeOfSrc: Type?,
        context: JsonSerializationContext
    ): JsonElement {
        return src.typedJsonObject { add("message", context.serialize(src.messageValue)) }
    }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext
    ): ApplyMessage {
        return ApplyMessage(context.deserialize(json.asJsonObject["message"], Value::class.java))
    }
}

object NotifyComponentAttachedAdapter : JsonSerializer<NotifyComponentAttached>,
    JsonDeserializer<NotifyComponentAttached> {

    override fun serialize(
        src: NotifyComponentAttached,
        typeOfSrc: Type?,
        context: JsonSerializationContext
    ): JsonElement {
        return src.typedJsonObject { add("state", context.serialize(src.state)) }
    }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext
    ): NotifyComponentAttached {
        return NotifyComponentAttached(
            context.deserialize(
                json.asJsonObject["state"],
                Value::class.java
            )
        )
    }
}

object ActionAppliedAdapter : JsonSerializer<ActionApplied>, JsonDeserializer<ActionApplied> {

    override fun serialize(
        src: ActionApplied,
        typeOfSrc: Type?,
        context: JsonSerializationContext
    ): JsonElement {
        return src.typedJsonObject { add("id", context.serialize(src.id)) }
    }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext
    ): ActionApplied {
        return ActionApplied(context.deserialize(json.asJsonObject["id"], UUID::class.java))
    }
}

object UUIDAdapter : JsonSerializer<UUID>, JsonDeserializer<UUID> {

    override fun serialize(
        src: UUID,
        typeOfSrc: Type?,
        context: JsonSerializationContext
    ): JsonElement {
        return JsonPrimitive(src.toString())
    }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext
    ): UUID {
        return UUID.fromString(json.asString)
    }
}

object ComponentIdAdapter : JsonSerializer<ComponentId>, JsonDeserializer<ComponentId> {

    override fun serialize(
        src: ComponentId,
        typeOfSrc: Type?,
        context: JsonSerializationContext
    ): JsonElement {
        return JsonPrimitive(src.id)
    }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext
    ): ComponentId {
        return ComponentId(json.asString)
    }
}

private inline fun Any.typedJsonObject(
    typePropertyName: String = "type",
    block: JsonObject.() -> Unit
): JsonObject {
    return JsonObject()
        .apply { addProperty(typePropertyName, this@typedJsonObject::class.java.serializeName) }
        .apply(block)
}


@Suppress("UNCHECKED_CAST")
private fun String.wrapperType() = Class.forName(this) as Class<out Value<*>>

@Suppress("UNCHECKED_CAST")
private fun String.clazz() = Class.forName(this)

private fun JsonObject.getDeserializationTargetElement(wrapperType: Class<out Value<*>>): JsonElement {
    return if (PrimitiveWrapper::class.java.isAssignableFrom(wrapperType)) this["value"] else this
}

private inline val Class<out Value<*>>.payloadFieldName: String
    get() = if (PrimitiveWrapper::class.java.isAssignableFrom(this) || Null::class.java.isAssignableFrom(
            this
        )
    ) "value" else "properties"