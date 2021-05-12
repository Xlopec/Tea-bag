/*
 * MIT License
 *
 * Copyright (c) 2021. Maksym Oliinyk.
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

package com.max.reader.misc

// TODO remove unused
typealias Either2<T, U> = Either3<T, U, Nothing>
typealias Either3<T, U, R> = Either4<T, U, R, Nothing>
typealias Either4<T, U, R, V> = Either5<T, U, R, V, Nothing>

sealed class Either5<out T, out U, out K, out R, out F>

data class E0<T>(
    val l: T,
) : Either5<T, Nothing, Nothing, Nothing, Nothing>()

data class E1<U>(
    val r: U,
) : Either5<Nothing, U, Nothing, Nothing, Nothing>()

data class E2<K>(
    val k: K,
) : Either5<Nothing, Nothing, K, Nothing, Nothing>()

data class E3<R>(
    val r: R,
) : Either5<Nothing, Nothing, Nothing, R, Nothing>()

data class E4<R>(
    val r: R,
) : Either5<Nothing, Nothing, Nothing, Nothing, R>()
