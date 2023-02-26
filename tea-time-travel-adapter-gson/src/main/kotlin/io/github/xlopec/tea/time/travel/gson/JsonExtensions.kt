package io.github.xlopec.tea.time.travel.gson

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken

internal fun JsonObject(
    type: TypeToken<*>,
    value: JsonElement,
) = JsonObject().apply {
    addProperty(SyntheticType, type)
    add(SyntheticValue, value)
}

internal fun JsonObject.addProperty(name: String, type: TypeToken<*>) {
    addProperty(name, type.rawType.name)
}

@Suppress("UNCHECKED_CAST")
internal fun <T> TypeToken(
    name: String,
): TypeToken<T> = TypeToken.get(Class.forName(name)) as TypeToken<T>

internal inline val JsonObject.syntheticType: TypeToken<Any?>
    get() = TypeToken(this[SyntheticType].asString)

internal inline val JsonObject.syntheticValue: JsonElement
    get() = this[SyntheticValue]

internal val JsonObject.nonSyntheticValue: JsonElement
    get() {
        require(isSyntheticObject) { "can't calculate value for non synthetic object, was $this" }
        return if (syntheticValue.isJsonArray) {
            // removes metadata from array elements
            val jsArray = syntheticValue.asJsonArray

            for (i in 0 until jsArray.size()) {
                val rawElement = jsArray[i]
                // remove metadata if needed
                val sanitizedElement = if (rawElement.isJsonObject && rawElement.asJsonObject.isSyntheticObject) {
                    rawElement.asJsonObject.syntheticValue
                } else {
                    rawElement
                }
                jsArray.set(i, sanitizedElement)
            }
            jsArray
        } else {
            syntheticValue
        }
    }

internal inline val JsonObject.isSyntheticObject: Boolean
    get() = has(SyntheticType) && has(SyntheticValue)