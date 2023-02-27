@file:Suppress("FunctionName")

package io.github.xlopec.tea.time.travel.gson

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken

internal fun SyntheticWrapper(
    type: TypeToken<*>,
    value: JsonElement,
) = JsonObject().apply {
    addMetadata(type)
    add(SyntheticValue, value)
}

public fun JsonObject.addMetadata(
    type: TypeToken<*>,
) {
    add(
        MetaData,
        JsonObject().apply {
            addTypeProperty(type)
        }
    )
}

public fun JsonObject.addMetadata(
    cl: Class<*>,
) {
    add(
        MetaData,
        JsonObject().apply {
            addTypeProperty(cl)
        }
    )
}

public fun JsonObject.addMetadata(
    typeName: String,
) {
    add(
        MetaData,
        JsonObject().apply {
            addProperty(SyntheticType, typeName)
        }
    )
}

public fun JsonObject.addTypeProperty(
    type: TypeToken<*>,
) {
    addTypeProperty(type.rawType)
}

public fun JsonObject.addTypeProperty(
    type: Class<*>,
) {
    addProperty(SyntheticType, type.name)
}

/*@Suppress("UNCHECKED_CAST")
internal fun <T> TypeToken(
    name: String,
): TypeToken<T> = TypeToken.get(Class.forName(name)) as TypeToken<T>*/

public inline val JsonObject.rawSyntheticType: String
    get() = this[SyntheticType].asString

internal inline val JsonObject.syntheticValue: JsonElement
    get() = this[SyntheticValue]

internal val JsonObject.nonSyntheticValue: JsonElement
    get() {
        require(isSyntheticObject) { "can't extract value for non synthetic object, was $this" }
        return if (syntheticValue.isJsonArray) {
            // removes metadata from array elements
            val array = syntheticValue.asJsonArray

            for (i in 0 ..< array.size()) {
                val rawElement = array[i]
                // removes metadata only if needed
                val sanitizedElement = if (rawElement.isJsonObject && rawElement.asJsonObject.isSyntheticObject) {
                    rawElement.asJsonObject.syntheticValue
                } else {
                    continue
                }
                array.set(i, sanitizedElement)
            }
            array
        } else {
            syntheticValue
        }
    }

internal inline val JsonObject.isSyntheticObject: Boolean
    get() = has(MetaData) && has(SyntheticValue)
