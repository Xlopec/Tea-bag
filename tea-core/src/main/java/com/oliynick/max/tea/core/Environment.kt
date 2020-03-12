@file:Suppress("FunctionName")

package com.oliynick.max.tea.core

import com.oliynick.max.tea.core.component.Resolver
import com.oliynick.max.tea.core.component.Updater
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Dependencies holder
 */
data class Env<M, S, C>(
    inline val initializer: Initializer<S, C>,
    inline val resolver: Resolver<C, M>,
    inline val updater: Updater<M, S, C>,
    val io: CoroutineDispatcher = Dispatchers.IO,
    val computation: CoroutineDispatcher = Dispatchers.Unconfined
)

class EnvBuilder<M, S, C>(
    var initializer: Initializer<S, C>,
    var resolver: Resolver<C, M>,
    var updater: Updater<M, S, C>,
    var io: CoroutineDispatcher = Dispatchers.IO,
    var computation: CoroutineDispatcher = Dispatchers.Unconfined
)

fun <M, S, C> Env(
    initializer: Initializer<S, C>,
    resolver: Resolver<C, M>,
    updater: Updater<M, S, C>,
    config: EnvBuilder<M, S, C>.() -> Unit = {}
) = EnvBuilder(initializer, resolver, updater)
    .apply(config)
    .toEnv()

fun <M, S, C> Env(
    initialState: S,
    resolver: Resolver<C, M>,
    updater: Updater<M, S, C>,
    vararg initialCommands: C,
    config: EnvBuilder<M, S, C>.() -> Unit = {}
) = Env(
    Initializer(
        initialState,
        setOf(*initialCommands)
    ), resolver, updater, config
)

fun <M, S, C> Env(
    initialState: S,
    resolver: Resolver<C, M>,
    updater: Updater<M, S, C>,
    initialCommands: Set<C>,
    config: EnvBuilder<M, S, C>.() -> Unit = {}
) = Env(
    Initializer(
        initialState,
        initialCommands
    ), resolver, updater, config
)

fun <M, S, C> EnvBuilder<M, S, C>.toEnv(): Env<M, S, C> =
    Env(initializer, resolver, updater, io, computation)