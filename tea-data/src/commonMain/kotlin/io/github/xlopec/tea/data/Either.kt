/*
 * MIT License
 *
 * Copyright (c) 2022. Maksym Oliinyk.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

@file:Suppress("FunctionName")

package io.github.xlopec.tea.data

import kotlin.jvm.JvmName

public sealed interface Either<out L, out R>

public data class Left<L>(
    val value: L
) : Either<L, Nothing>

public data class Right<R>(
    val value: R
) : Either<Nothing, R>

public inline fun <L, R> Either(
    ifSuccess: () -> L,
    ifFailure: (th: Throwable) -> R
): Either<L, R> = Either(ifSuccess).mapR(ifFailure)

public inline fun <L> Either(
    ifSuccess: () -> L,
): Either<L, Throwable> =
    runCatching { Left(ifSuccess()) }
        .getOrElse(::Right)

public inline fun <L> Left(
    ifSuccess: () -> L,
): Left<L> = Left(ifSuccess())

@JvmName("LeftUnit")
public inline fun Left(
    ifSuccess: () -> Unit,
): Left<Nothing?> {
    ifSuccess()
    return Left(null)
}

public inline fun <L, R, T> Either<L, R>.fold(
    left: (L) -> T,
    right: (R) -> T
): T =
    when (this) {
        is Left -> left(value)
        is Right -> right(value)
    }

public inline fun <L, R, T, F> Either<L, R>.bimap(
    left: (L) -> T,
    right: (R) -> F
): Either<T, F> =
    when (this) {
        is Left -> Left(left(value))
        is Right -> Right(right(value))
    }

public inline fun <L, R, T> Either<L, R>.mapL(
    left: (L) -> T,
): Either<T, R> = bimap(left) { it }

public inline fun <L, R, T> Either<L, R>.mapR(
    right: (R) -> T
): Either<L, T> = bimap({ it }, right)

//@OptIn(ExperimentalContracts::class)
public operator fun <L> Either<L, *>.component1(): L? {
    // contracts aren't supported for operator functions,
    // check for more info https://youtrack.jetbrains.com/issue/KT-32313
    /*contract {
        returnsNotNull() implies (this is Left<*>)
    }*/

    return (this as? Left)?.value
}

//@OptIn(ExperimentalContracts::class)
public operator fun <R> Either<*, R>.component2(): R? {
    // contracts aren't supported for operator functions,
    // check for more info https://youtrack.jetbrains.com/issue/KT-32313
    /*contract {
        returnsNotNull() implies (this is Right<*>)
    }*/

    return (this as? Right)?.value
}