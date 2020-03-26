package com.oliynick.max.tea.core.debug.app.domain

sealed class Either<out L, out R>

data class Left<L>(val l: L) : Either<L, Nothing>()

data class Right<R>(val r: R) : Either<Nothing, R>()

inline fun <L, R, T> Either<L, R>.fold(
    left: (L) -> T,
    right: (R) -> T
): T =
    when (this) {
        is Left -> left(l)
        is Right -> right(r)
    }
