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
