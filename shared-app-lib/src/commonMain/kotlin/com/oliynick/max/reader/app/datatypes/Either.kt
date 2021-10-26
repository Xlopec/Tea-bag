@file:Suppress("FunctionName")

package com.oliynick.max.reader.app.datatypes

// todo do something with bunch of Eithers
sealed class Either<out L, out R>

data class Left<L>(
    val l: L
) : Either<L, Nothing>()

data class Right<R>(
    val r: R
) : Either<Nothing, R>()

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
        is Left -> left(l)
        is Right -> right(r)
    }