package com.oliynick.max.tea.core.debug.gson.serialization.serializer

import com.google.gson.*
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

object MapDeserializer : JsonDeserializer<Map<*, *>> {

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): Map<*, *> {

        val genericKeyArgType = (typeOfT as ParameterizedType).actualTypeArguments[1] as Class<*>

        return json.asJsonObject.entrySet()
            .associate { (k, v) ->
                (if (k == "null") null else k) to context.deserialize<Any?>(
                    v,
                    genericKeyArgType
                )
            }
    }

}
