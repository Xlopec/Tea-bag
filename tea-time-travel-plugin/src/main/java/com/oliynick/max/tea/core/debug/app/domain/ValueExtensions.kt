package com.oliynick.max.tea.core.debug.app.domain

val PrimitiveNumbers = setOf(
    Byte::class.java,
    Short::class.java,
    java.lang.Short::class.java,
    Integer::class.java,
    Long::class.java,
    java.lang.Long::class.java,
    Float::class.java,
    java.lang.Float::class.java,
    Double::class.java,
    java.lang.Double::class.java,
    Int::class.java
)

inline val Value.primitiveTypeName: String?
    get() = when (this) {
        is NumberWrapper -> primitiveTypeName
        is CharWrapper -> primitiveTypeName
        is StringWrapper -> primitiveTypeName
        is BooleanWrapper -> primitiveTypeName
        Null, is CollectionWrapper, is Ref -> null
    }

inline val NumberWrapper.primitiveTypeName: String
    get() = if (value.javaClass in PrimitiveNumbers) value.javaClass.name else "number"

inline val CharWrapper.primitiveTypeName: String
    get() = value.javaClass.name

inline val StringWrapper.primitiveTypeName: String
    get() = value.javaClass.name

inline val BooleanWrapper.primitiveTypeName: String
    get() = value.javaClass.name
