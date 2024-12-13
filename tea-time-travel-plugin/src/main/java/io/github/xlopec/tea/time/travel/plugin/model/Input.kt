package io.github.xlopec.tea.time.travel.plugin.model

import arrow.core.Either

data class Input<out E, out V>(
    val input: String,
    val value: Either<E, V>,
)

val Either<*, *>.isValid: Boolean
    get() = this is Either.Right

val Either<*, *>.isInvalid: Boolean
    get() = !isValid
