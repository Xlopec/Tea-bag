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

import kotlin.contracts.contract

sealed class Validated<out T> {
    abstract val input: String
}

data class Valid<out T>(
    override val input: String,
    val t: T
) : Validated<T>()

data class Invalid(
    override val input: String,
    val message: String
) : Validated<Nothing>()

inline fun <T, R> Validated<T>.fold(
    valid: (Valid<T>) -> R,
    invalid: (Invalid) -> R
): R =
    when (this) {
        is Valid -> valid(this)
        is Invalid -> invalid(this)
    }

inline fun <T, R> Validated<T>.map(
    mapper: (T) -> R
): Validated<R> =
    when (this) {
        is Valid -> Valid(input, mapper(t))
        is Invalid -> this
    }

fun <T> Validated<T>.isValid(): Boolean {
    contract {
        returns(true) implies (this@isValid is Valid<T>)
        returns(false) implies (this@isValid is Invalid)
    }
    return this is Valid
}

inline val <T> Validated<T>?.value: T?
    get() = if (this is Valid) t else null
