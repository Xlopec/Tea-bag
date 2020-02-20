@file:Suppress("FunctionName")

package com.oliynick.max.tea.core

typealias Initializer<S, C> = suspend () -> Initial<S, C>

fun <S, C> Initializer(s: S, commands: Set<C>): Initializer<S, C> =
    { Initial(s, commands) }

fun <S, C> Initializer(s: S, vararg commands: C): Initializer<S, C> =
    Initializer(s, setOf(*commands))

fun <S, C> Initializer(s: S): Initializer<S, C> =
    Initializer(s, emptySet())