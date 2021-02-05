@file:Suppress("FunctionName")
@file:OptIn(UnstableApi::class)

package com.oliynick.max.tea.core

import com.oliynick.max.tea.core.component.Resolver
import com.oliynick.max.tea.core.component.Updater
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted

/**
 * Environment is an application component responsible for holding application dependencies
 *
 * @param initializer initializer to be used to provide initial values for application
 * @param resolver resolver to be used to resolve messages from commands
 * @param updater updater to be used to compute a new state with set of commands to execute
 * @param scope scope in which the sharing coroutine is started
 * @param io coroutine dispatcher which is used to execute side effects by [resolver]
 * @param computation coroutine dispatcher which is used to wrap [updater]'s computations
 * @param shareOptions sharing options, see [shareIn][kotlinx.coroutines.flow.shareIn] for more info
 * @param M message type
 * @param S state type
 * @param C command type
 */
data class Env<M, S, C>(
    val initializer: Initializer<S, C>,
    val resolver: Resolver<C, M>,
    val updater: Updater<M, S, C>,
    // todo: group to reduce number of arguments
    val scope: CoroutineScope,
    val io: CoroutineDispatcher = Dispatchers.IO,
    val computation: CoroutineDispatcher = Dispatchers.Unconfined,
    val shareOptions: ShareOptions = ShareStateWhileSubscribed,
)

/**
 * Share options used to configure the sharing coroutine
 *
 * @param started sharing strategy
 * @param replay number of states to be replayed to the downstream subscribers
 * @see [SharingStarted]
 */
data class ShareOptions(
    val started: SharingStarted,
    val replay: UInt,
)

@UnstableApi
val ShareStateWhileSubscribed = ShareOptions(SharingStarted.WhileSubscribed(), 1U)
