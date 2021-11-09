@file:Suppress("FunctionName")

package com.oliynick.max.reader.app.datatypes

// todo do something with bunch of Eithers
sealed interface Either<out L, out R>

data class Left<L>(
    val value: L
) : Either<L, Nothing>

data class Right<R>(
    val value: R
) : Either<Nothing, R>

inline fun <L, R> Either(
    ifSuccess: () -> L,
    ifFailure: (th: Throwable) -> R
): Either<L, R> =
    runCatching { Left(ifSuccess()) }
        .getOrElse { th -> Right(ifFailure(th)) }

inline fun <L, R, T> Either<L, R>.fold(
    left: (L) -> T,
    right: (R) -> T
): T =
    when (this) {
        is Left -> left(value)
        is Right -> right(value)
    }

//@OptIn(ExperimentalContracts::class)
operator fun <L> Either<L, *>.component1(): L? {
    // contracts aren't supported for operator functions,
    // check for more info https://youtrack.jetbrains.com/issue/KT-32313
    /*contract {
        returnsNotNull() implies (this is Left<*>)
    }*/

    return (this as? Left)?.value
}

//@OptIn(ExperimentalContracts::class)
operator fun <R> Either<*, R>.component2(): R? {
    // contracts aren't supported for operator functions,
    // check for more info https://youtrack.jetbrains.com/issue/KT-32313
    /*contract {
        returnsNotNull() implies (this is Right<*>)
    }*/

    return (this as? Right)?.value
}