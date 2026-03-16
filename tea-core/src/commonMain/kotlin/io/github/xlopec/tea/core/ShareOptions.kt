package io.github.xlopec.tea.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn

/**
 * Defines transformation of an upstream [Flow] into a resulting Component's [Flow].
 *
 * This function is used for configuring how the upstream flow should be shared,
 * with particular lifecycle behavior, within a given [CoroutineScope].
 *
 * @param T the type of elements emitted by the [Flow]
 * @return a configured [Flow] with the desired behavior
 */
public typealias ShareOptions<T> = (scope: CoroutineScope, upstream: Flow<T>) -> Flow<T>

/**
 * Provides sharing options for a state flow, utilizing the `shareIn` operator with a `WhileSubscribed` strategy.
 *
 * @param started Determines the sharing strategy, defaulting to `SharingStarted.WhileSubscribed()`.
 * @param replay The number of replayed values, defaulting to 1.
 * @return A lambda function that applies the `shareIn` operator to the upstream flow using the provided parameters.
 */
@Suppress("FunctionName")
public fun <T> ShareStateWhileSubscribed(
    started: SharingStarted = SharingStarted.WhileSubscribed(),
    replay: UInt = 1U,
): ShareOptions<T> = { scope, upstream -> upstream.shareIn(scope = scope, started = started, replay = replay.toInt()) }
