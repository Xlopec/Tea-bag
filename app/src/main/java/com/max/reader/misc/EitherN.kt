package com.max.reader.misc

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
