/*
 * Copyright (C) 2021. Maksym Oliinyk.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.xlopec.tea.time.travel.plugin.model

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
