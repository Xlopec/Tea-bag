package com.oliynick.max.elm.core.component

sealed class Snapshot<out M, out S, out C> {
    abstract val currentState: S
    abstract val commands: Set<C>
}

data class Initial<out S, out C>(
    override val currentState: S,
    override val commands: Set<C>
) : Snapshot<Nothing, S, C>()

data class Regular<out M, out S, out C>(
    override val currentState: S,
    override val commands: Set<C>,
    val previousState: S,
    val message: M
) : Snapshot<M, S, C>()