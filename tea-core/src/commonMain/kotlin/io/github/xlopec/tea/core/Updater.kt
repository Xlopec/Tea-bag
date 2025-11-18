/*
 * MIT License
 *
 * Copyright (c) 2022. Maksym Oliinyk.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.xlopec.tea.core

/**
 * Updater is a regular **pure** function that accepts an incoming message and a state and calculates
 * an [Update] that contains a new state and, optionally, a set of commands to be resolved.
 *
 * ### Exceptions
 *
 * Any exception that happens inside this function will be delivered to a [Component]'s scope and handled
 * by it.
 *
 * @param M message type
 * @param S state type
 * @param C command type
 * @param message incoming message
 * @param state current state
 */
public typealias Updater<M, S, C> = (message: M, state: S) -> Update<S, C>

/**
 * Alias for Kotlin's [Pair]. It can be created using the [command] extensions.
 *
 * @param S state type
 * @param C command type. There's **NO GUARANTEE** of command ordering; they can be
 * executed in any order. This implies that calculation correctness mustn't depend on the ordering.
 */
public typealias Update<S, C> = Pair<S, Set<C>>

/**
 * Combines a state with a command.
 *
 * @receiver state to combine with a command
 * @param S state type
 * @param C command type
 * @param command command to combine with the state
 * @return [Update] instance with the given state and a set consisting of a single command
 */
public infix fun <S, C> S.command(
    command: C,
): Update<S, C> = this to setOf(command)

/**
 * Combines a state with a single command provider.
 *
 * @receiver state to combine with a command
 * @param S state type
 * @param C command type
 * @param command command provider to combine with the state
 * @return [Update] instance with the given state and a set consisting of a single command
 */
public inline infix fun <S, C> S.command(
    command: S.() -> C,
): Update<S, C> = this to setOf(run(command))

/**
 * Combines a state with two commands.
 *
 * @receiver state to combine with commands
 * @param S state type
 * @param C command type
 * @param first the first command to combine with the state
 * @param second the second command to combine with the state
 * @return [Update] instance with the given state and a set of commands
 */
public fun <S, C> S.command(
    first: C,
    second: C,
): Update<S, C> = this to setOf(first, second)

/**
 * Combines a state with three commands.
 *
 * @receiver state to combine with commands
 * @param S state type
 * @param C command type
 * @param first the first command to combine with the state
 * @param second the second command to combine with the state
 * @param third the third command to combine with the state
 * @return [Update] instance with the given state and a set of commands
 */
public fun <S, C> S.command(
    first: C,
    second: C,
    third: C,
): Update<S, C> =
    this to setOf(first, second, third)

/**
 * Combines a state with multiple commands.
 *
 * @receiver state to combine with commands
 * @param S state type
 * @param C command type
 * @param commands commands to combine with the state
 * @return [Update] instance with the given state and a set of commands
 */
public fun <S, C> S.command(
    vararg commands: C,
): Update<S, C> = this command setOf(*commands)

/**
 * Combines a state with a set of commands.
 *
 * @receiver state to combine with commands
 * @param S state type
 * @param C command type
 * @param commands commands to combine with the state
 * @return [Update] instance with the given state and the set of commands
 */
public infix fun <S, C> S.command(
    commands: Set<C>,
): Update<S, C> = this to commands

/**
 * Combines a state with an empty set of commands.
 *
 * @receiver state to combine with an empty set of commands
 * @param S state type
 * @return [Update] instance with the given state and an empty set of commands
 */
public fun <S> S.noCommand(): Update<S, Nothing> = this to setOf()

/**
 * Appends a command to an update.
 *
 * @receiver update to append a command to
 * @param S state type
 * @param C command type
 * @param command command to append
 * @return [Update] instance with the command appended
 */
public operator fun <S, C> Update<S, C>.plus(
    command: C,
): Update<S, C> {
    val (state, commands) = this

    return state command commands + command
}

/**
 * Appends commands to an update.
 *
 * @receiver update to append commands to
 * @param S state type
 * @param C command type
 * @param additionalCommands commands to append
 * @return [Update] instance with the commands appended
 */
public operator fun <S, C> Update<S, C>.plus(
    additionalCommands: Collection<C>,
): Update<S, C> {
    val (state, commands) = this

    return state command commands + additionalCommands
}
