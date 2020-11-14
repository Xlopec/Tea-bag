@file:Suppress("FunctionName")
@file:OptIn(UnstableApi::class)

package com.oliynick.max.tea.core

import com.oliynick.max.tea.core.component.Resolver
import com.oliynick.max.tea.core.component.Updater
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Environment is an application component responsible for holding application dependencies
 *
 * @param initializer initializer to be used to provide initial values for application
 * @param resolver resolver to be used to resolve messages from commands
 * @param updater updater to be used to compute a new state with set of commands to execute
 * @param io coroutine dispatcher to be used in [resolver]
 * @param computation coroutine dispatcher to be used in [updater]
 * @param M message type
 * @param S state type
 * @param C command type
 */
data class Env<M, S, C>(
    val initializer: Initializer<S, C>,
    val resolver: Resolver<C, M>,
    val updater: Updater<M, S, C>,
    val io: CoroutineDispatcher = Dispatchers.IO,
    val computation: CoroutineDispatcher = Dispatchers.Unconfined,
)

/**
 * Builder to configure and create a corresponding instance of [Env]
 *
 * @param initializer initializer to be used to provide initial values for application
 * @param resolver resolver to be used to resolve messages from commands
 * @param updater updater to be used to compute a new state with set of commands to execute
 * @param io coroutine dispatcher to be used in [resolver]
 * @param computation coroutine dispatcher to be used in [updater]
 * @param M message type
 * @param S state type
 * @param C command type
 */
class EnvBuilder<M, S, C>(
    var initializer: Initializer<S, C>,
    var resolver: Resolver<C, M>,
    var updater: Updater<M, S, C>,
    var io: CoroutineDispatcher = Dispatchers.IO,
    var computation: CoroutineDispatcher = Dispatchers.Unconfined
)

/**
 * Configures and creates [Env]
 *
 * @param initializer initializer to be used to provide initial values for application
 * @param resolver resolver to be used to resolve messages from commands
 * @param updater updater to be used to compute a new state with set of commands to execute
 * @param config block to configure environment
 * @param M message type
 * @param S state type
 * @param C command type
 */
fun <M, S, C> Env(
    initializer: Initializer<S, C>,
    resolver: Resolver<C, M>,
    updater: Updater<M, S, C>,
    config: EnvBuilder<M, S, C>.() -> Unit = {}
) = EnvBuilder(initializer, resolver, updater)
    .apply(config)
    .toEnv()

/**
 * Configures and creates [Env]
 *
 * @param initialState initial state to be used by application
 * @param initialCommands initial commands to be used by application
 * @param resolver resolver to be used to resolve messages from commands
 * @param updater updater to be used to compute a new state with set of commands to execute
 * @param config block to configure environment
 * @param M message type
 * @param S state type
 * @param C command type
 */
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

/**
 * Configures and creates [Env]
 *
 * @param initialState initial state to be used by application
 * @param initialCommands initial commands to be used by application
 * @param resolver resolver to be used to resolve messages from commands
 * @param updater updater to be used to compute a new state with set of commands to execute
 * @param config block to configure environment
 * @param M message type
 * @param S state type
 * @param C command type
 */
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

@UnstableApi
fun <M, S, C> EnvBuilder<M, S, C>.toEnv(): Env<M, S, C> =
    Env(initializer, resolver, updater, io, computation)
