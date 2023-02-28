@file:Suppress("FunctionName")

package io.github.xlopec.tea.time.travel.gson

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import io.github.xlopec.tea.time.travel.gson.metadata.*

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
        Metadata,
        JsonObject().apply {
            addTypeProperty(type)
        }
    )
}

public fun JsonObject.addMetadata(
    cl: Class<*>,
) {
    add(
        Metadata,
        JsonObject().apply {
            addTypeProperty(cl)
        }
    )
}

@Deprecated("incorrect")
public fun JsonObject.addMetadata(
    typeName: String,
) {
    add(
        Metadata,
        JsonObject().apply {
            addProperty(SimpleType1, typeName)
        }
    )
}

public fun JsonObject.addTypeProperty(
    type: TypeToken<*>,
) {
    addProperty(SimpleType1, type.rawType.name)
    add(FullType, type.type.toJsonTypeTree())
}

@Deprecated("use type token overload")
public fun JsonObject.addTypeProperty(
    clazz: Class<*>,
) {
    addTypeProperty(TypeToken.get(clazz))
}

public inline val JsonObject.rawSyntheticType: String
    get() = this[SimpleType1].asString

internal inline val JsonObject.syntheticValue: JsonElement
    get() = this[SyntheticValue]

internal val JsonObject.flattenMap: JsonObject
    get() {
        val js = JsonObject()
        entrySet().forEach { (k, v) ->

            if (k != "@meta") {

            }

            // js.add(k, )
        }
        TODO()
    }

/**
 * Flattens synthetic values but keeps metadata for type token lookup
 */
internal fun JsonObject.flattenSynthetics(): JsonElement {
    return if (isSyntheticObject) {
        nonSyntheticValue
    } else {
        val asMap = asMap()
        for (entry in asMap.entries) {

            if (entry.key != Metadata) {
                val jsonElement = entry.value
                val flatten = if (jsonElement.isJsonObject) {
                    jsonElement.asJsonObject.flattenSynthetics()
                } else if(jsonElement.isJsonArray) {
                    val array = jsonElement.asJsonArray

                    for (i in 0..<array.size()) {
                        val rawElement = array[i]
                        // removes metadata only if needed
                        val sanitizedElement = if (rawElement.isJsonObject) {
                            rawElement.asJsonObject.flattenSynthetics()
                        } else {
                            continue
                        }
                        array.set(i, sanitizedElement)
                    }
                    array
                } else {
                    jsonElement
                }

                entry.setValue(flatten)
            }
        }

        remove(Metadata)
        this
    }
}

internal val JsonObject.nonSyntheticValue: JsonElement
    get() {
        require(isSyntheticObject) { "can't extract value for non synthetic object, was $this" }
        return if (syntheticValue.isJsonArray) {
            // removes metadata from array elements
            val array = syntheticValue.asJsonArray

            for (i in 0..<array.size()) {
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
    get() = hasMetadata && has(SyntheticValue)

internal inline val JsonObject.hasMetadata: Boolean
    get() = has(Metadata)