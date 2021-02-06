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
