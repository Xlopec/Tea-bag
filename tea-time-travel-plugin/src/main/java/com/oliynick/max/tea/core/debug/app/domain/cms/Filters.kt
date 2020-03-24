package com.oliynick.max.tea.core.debug.app.domain.cms

/**
 * Filters given [value] recursively. The value will be taken if it
 * or any its child value matches regex.
 * Any non-matching siblings of the current value will be filtered out
 */
fun Regex?.applyTo(
    value: Value
): Value? =
    when {
        this == null || matchesAsPrimitive(value) || (value is Null && matches("null")) -> value
        value is CollectionWrapper -> applyToWrapper(value)
        value is Ref -> applyToRef(value)
        else -> null
    }

private fun Regex.matchesAsPrimitive(
    value: Value
): Boolean {

    val typeName: String? = when (value) {
        is IntWrapper -> value.value.javaClass.name
        is ByteWrapper -> value.value.javaClass.name
        is ShortWrapper -> value.value.javaClass.name
        is CharWrapper -> value.value.javaClass.name
        is LongWrapper -> value.value.javaClass.name
        is DoubleWrapper -> value.value.javaClass.name
        is FloatWrapper -> value.value.javaClass.name
        is StringWrapper -> value.value.javaClass.name
        is BooleanWrapper -> value.value.javaClass.name
        Null, is CollectionWrapper, is Ref -> null
    }

    return typeName?.let { name -> matches(name) } == true
}

private fun Regex.applyToRef(
    ref: Ref
): Ref? {

    fun applyToProp(
        property: Property
    ): Property? =
        if (matches(property.name)) property
        else applyTo(property.v)
            ?.let { filteredValue ->
                Property(
                    property.name,
                    filteredValue
                )
            }

    return if (matches(ref.type.name)) ref
    else ref.properties.mapNotNullTo(HashSet(ref.properties.size), ::applyToProp)
        .takeIf { filteredProps -> filteredProps.isNotEmpty() }
        ?.let { ref.copy(properties = it) }
}

private fun Regex.applyToWrapper(
    wrapper: CollectionWrapper
): CollectionWrapper? =
    wrapper.value
        .mapNotNull { v -> applyTo(v) }
        .takeIf { filtered -> filtered.isNotEmpty() }
        ?.let(::CollectionWrapper)
