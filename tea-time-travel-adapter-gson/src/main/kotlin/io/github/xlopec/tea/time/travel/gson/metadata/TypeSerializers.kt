package io.github.xlopec.tea.time.travel.gson.metadata

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.internal.`$Gson$Types`
import java.lang.reflect.GenericArrayType
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.WildcardType

private const val KindField = "kind"
private const val OwnerTypeField = "owner_type"
private const val RawTypeField = "raw_type"
private const val TypeArgsField = "type_args"
private const val UpperBoundsField = "upper_bounds"
private const val LowerBoundsField = "lower_bounds"
private const val ArrayTypeField = "array_type"
private const val NameField = "name"

private enum class Kind {
    Parameterized,
    Wildcard,
    GenericArray,
    Class
}

internal fun Type.toJsonTypeTree(): JsonObject =
    when (this) {
        is ParameterizedType -> toParameterizedTypeJsonTree()
        is WildcardType -> toWildcardTypeJsonTree()
        is GenericArrayType -> toGenericArrayTypeJsonTree()
        is Class<*> -> toTypeJsonTree()
        else -> error("unsupported type $this")
    }

internal fun JsonObject.fromJsonTypeTree(): Type {
    return when (Kind.valueOf(this[KindField].asString)) {
        Kind.Parameterized -> fromParameterizedTypeJsonTree()
        Kind.Wildcard -> fromWildcardTypeJsonTree()
        Kind.GenericArray -> fromGenericTypeJsonTree()
        Kind.Class -> fromClassJsonTree()
    }
}

private fun ParameterizedType.toParameterizedTypeJsonTree() =
    JsonObject {
        addProperty(KindField, Kind.Parameterized.toString())
        add(OwnerTypeField, ownerType?.toJsonTypeTree())
        add(RawTypeField, rawType.toJsonTypeTree())
        add(TypeArgsField, actualTypeArguments.toJsonTypeTree())
    }

private fun WildcardType.toWildcardTypeJsonTree() = JsonObject {
    addProperty(KindField, Kind.Wildcard.toString())
    add(UpperBoundsField, upperBounds.toJsonTypeTree())
    add(LowerBoundsField, lowerBounds.toJsonTypeTree())
}

private fun GenericArrayType.toGenericArrayTypeJsonTree() = JsonObject {
    addProperty(KindField, Kind.GenericArray.toString())
    add(ArrayTypeField, genericComponentType.toJsonTypeTree())
}

private fun Class<*>.toTypeJsonTree() = JsonObject {
    addProperty(KindField, Kind.Class.toString())
    addProperty(NameField, name)
}


private fun JsonObject.fromClassJsonTree(): Class<*> = Class.forName(this[NameField].asString)

private fun JsonObject.fromGenericTypeJsonTree(): GenericArrayType =
    `$Gson$Types`.arrayOf(this[ArrayTypeField].asJsonObject.fromJsonTypeTree())

private fun JsonObject.fromWildcardTypeJsonTree(): WildcardType =
    WildcardTypeImpl(
        this[UpperBoundsField].asJsonArray.map { it.asJsonObject.fromJsonTypeTree() },
        this[LowerBoundsField].asJsonArray.map { it.asJsonObject.fromJsonTypeTree() },
    )

private fun JsonObject.fromParameterizedTypeJsonTree(): ParameterizedType =
    `$Gson$Types`.newParameterizedTypeWithOwner(
        this[OwnerTypeField].takeIf { it.isJsonObject }?.asJsonObject?.fromJsonTypeTree(),
        this[RawTypeField].asJsonObject.fromJsonTypeTree(),
        *this[TypeArgsField].asJsonArray.map { it.asJsonObject.fromJsonTypeTree() }
    )

private inline fun <reified T> JsonArray.map(
    mapper: (JsonElement) -> T,
): Array<T> = Array(size()) { i -> mapper(get(i)) }

private inline fun JsonObject(
    builder: JsonObject.() -> Unit,
) = JsonObject().apply(builder)

private fun Array<Type>.toJsonTypeTree() = JsonArray(size).apply {
    this@toJsonTypeTree.forEach { type ->
        add(type.toJsonTypeTree())
    }
}