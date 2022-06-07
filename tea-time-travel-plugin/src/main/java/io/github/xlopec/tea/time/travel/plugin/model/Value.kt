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

import androidx.compose.runtime.Immutable

@JvmInline
value class Type private constructor(
    val name: String
) {
    companion object {
        fun of(
            name: String
        ) = Type(name)

        fun of(
            any: Any
        ) = of(any::class.java.name)
    }

    init {
        require(name.isNotEmpty())
    }
}

data class Property(
    val name: String,
    val v: Value
)

@Immutable
sealed interface Value

object Null : Value

@JvmInline
value class NumberWrapper(
    val value: Number
) : Value

@JvmInline
value class CharWrapper(
    val value: Char
) : Value

@JvmInline
value class StringWrapper(
    val value: String
) : Value

@JvmInline
value class BooleanWrapper private constructor(
    val value: Boolean
) : Value {

    companion object {
        private val TRUE = BooleanWrapper(true)
        private val FALSE = BooleanWrapper(false)

        fun of(
            value: Boolean
        ) = if (value) TRUE else FALSE
    }
}

@JvmInline
value class CollectionWrapper(
    val items: List<Value> = listOf()
) : Value {
    constructor(vararg items: Value) : this(listOf(*items))
}

data class Ref(
    val type: Type,
    val properties: Set<Property>
) : Value {
    constructor(type: Type, vararg properties: Property) : this(type, setOf(*properties))
}

inline val Value.isPrimitive: Boolean
    get() = when (this) {
        is CharWrapper,
        is NumberWrapper,
        is StringWrapper,
        is BooleanWrapper
        -> true
        Null, is CollectionWrapper, is Ref -> false
    }

inline val Value.stringValue: String?
    get() = when (this) {
        is BooleanWrapper -> value.toString()
        is CharWrapper -> value.toString()
        is StringWrapper -> value
        is NumberWrapper -> value.toString()
        Null -> null.toString()
        is CollectionWrapper -> null
        is Ref -> null
    }
