package com.oliynick.max.tea.core.component.internal

import com.oliynick.max.tea.core.*
import com.oliynick.max.tea.core.component.Resolver
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@InternalComponentApi
fun <M, S, C> Env<M, C, S>.upstream(
    messages: Flow<M>,
    snapshots: Flow<Initial<S, C>>
) = snapshots.flatMapConcat { startFrom -> compute(startFrom, messages) }

@InternalComponentApi
fun <M, S, C> Flow<Snapshot<M, S, C>>.downstream(
    input: Flow<M>,
    upstreamInput: Channel<M>
): Flow<Snapshot<M, S, C>> =
    channelFlow {
        @Suppress("NON_APPLICABLE_CALL_FOR_BUILDER_INFERENCE")
        onStart { launch { input.into(upstreamInput) } }
            .into(channel)
    }

@InternalComponentApi
fun <M, S, C> Env<M, C, S>.init(): Flow<Initial<S, C>> =
    flow { emit(initializer()) }

private fun <M, S, C> Env<M, C, S>.compute(
    startFrom: Initial<S, C>,
    messages: Flow<M>
): Flow<Snapshot<M, S, C>> =
    resolve(startFrom.commands).finishWith(messages)
        .foldFlatten<M, Snapshot<M, S, C>>(startFrom) { s, m -> computeNextSnapshot(s.currentState, m) }
        .startFrom(startFrom)

private fun <M, C, S> Env<M, C, S>.resolve(commands: Collection<C>): Flow<M> =
    flow { emitAll(resolver(commands).asFlow()) }

private suspend fun <M, C, S> Env<M, C, S>.computeNextSnapshotsRecursively(
    state: S,
    messages: Iterator<M>
): Flow<Snapshot<M, S, C>> {

    val message = messages.nextOrNull() ?: return emptyFlow()

    val (nextState, commands) = update(message, state)

    return computeNextSnapshotsRecursively(nextState, resolver(commands).iterator())
        .startFrom(Regular(nextState, commands, state, message))
}

private suspend fun <M, C, S> Env<M, C, S>.computeNextSnapshot(
    state: S,
    message: M
): Flow<Snapshot<M, S, C>> {
    // todo: we need to add possibility to return own versions
    //  of snapshots, e.g. user might be interested only in current
    //  version of state
    val (nextState, commands) = update(message, state)

    return computeNextSnapshotsRecursively(nextState, resolver(commands).iterator())
        .startFrom(Regular(nextState, commands, state, message))
}

private fun <E> Iterator<E>.nextOrNull() = if (hasNext()) next() else null

private suspend operator fun <C, M> Resolver<C, M>.invoke(commands: Collection<C>): Set<M> =
    commands.fold(LinkedHashSet(commands.size)) { acc, cmd -> acc.addAll(this(cmd)); acc }