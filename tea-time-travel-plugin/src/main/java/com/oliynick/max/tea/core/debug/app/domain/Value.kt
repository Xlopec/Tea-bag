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

package com.oliynick.max.tea.core.debug.app.domain

@JvmInline
value class Type private constructor(
    val name: String
) {
    companion object {
        fun of(
            name: String
        ): Type {
            require(name.isNotEmpty())
            return Type(name)
        }

        fun of(
            any: Any
        ) = of(any::class.java.name)
    }
}

data class Property(
    val name: String,
    val v: Value
)

sealed interface Value

object Null : Value

//todo replace by overloaded factory function

data class NumberWrapper(
    val value: Number
) : Value

data class CharWrapper(
    val value: Char
) : Value

data class StringWrapper(
    val value: String
) : Value

class BooleanWrapper private constructor(
    val value: Boolean
) : Value {

    companion object {
        private val TRUE by lazy(LazyThreadSafetyMode.NONE) {
            BooleanWrapper(
                true
            )
        }
        private val FALSE by lazy(LazyThreadSafetyMode.NONE) {
            BooleanWrapper(
                false
            )
        }

        fun of(
            value: Boolean
        ) = if (value) TRUE else FALSE
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BooleanWrapper

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun toString(): String {
        return "BooleanWrapper(value=$value)"
    }

}

data class CollectionWrapper(
    val items: List<Value>
) : Value

data class Ref(
    val type: Type,
    val properties: Set<Property>
) : Value

inline val Value.isPrimitive: Boolean
    get() = when (this) {
        is CharWrapper,
        is NumberWrapper,
        is StringWrapper,
        is BooleanWrapper
        -> true
        Null, is CollectionWrapper, is Ref -> false
    }

