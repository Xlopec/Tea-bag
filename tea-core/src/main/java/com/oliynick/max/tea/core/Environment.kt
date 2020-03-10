@file:Suppress("FunctionName")

package com.oliynick.max.tea.core

import com.oliynick.max.tea.core.component.Resolver
import com.oliynick.max.tea.core.component.Update
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Dependencies holder
 */
data class Env<M, S, C>(
    inline val initializer: Initializer<S, C>,
    inline val resolver: Resolver<C, M>,
    inline val update: Update<M, S, C>,
    val io: CoroutineDispatcher
)

class EnvBuilder<M, S, C>(
    var initializer: Initializer<S, C>,
    var resolver: Resolver<C, M>,
    var update: Update<M, S, C>,
    var io: CoroutineDispatcher = Dispatchers.IO
) {
    constructor(env: Env<M, S, C>) : this(
        env.initializer,
        env.resolver,
        env.update,
        env.io
    )
}

fun <M, S, C> Env(
    initializer: Initializer<S, C>,
    resolver: Resolver<C, M>,
    update: Update<M, S, C>,
    config: EnvBuilder<M, S, C>.() -> Unit = {}
) = EnvBuilder(initializer, resolver, update)
    .apply(config)
    .toEnv()

fun <M, S, C> Env(
    initialState: S,
    resolver: Resolver<C, M>,
    update: Update<M, S, C>,
    vararg initialCommands: C,
    config: EnvBuilder<M, S, C>.() -> Unit = {}
) = Env(
    Initializer(
        initialState,
        setOf(*initialCommands)
    ), resolver, update, config
)

fun <M, S, C> Env(
    initialState: S,
    resolver: Resolver<C, M>,
    update: Update<M, S, C>,
    initialCommands: Set<C>,
    config: EnvBuilder<M, S, C>.() -> Unit = {}
) = Env(
    Initializer(
        initialState,
        initialCommands
    ), resolver, update, config
)

fun <M, S, C> EnvBuilder<M, S, C>.toEnv(): Env<M, S, C> =
    Env(initializer, resolver, update, io)