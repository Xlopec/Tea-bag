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
 * Updater is just a regular **pure** function that accepts incoming message, state and calculates
 * a [pair][Update] that contains a new state and, possibly, empty set of commands to be resolved
 *
 * ### Exceptions
 *
 * Any exception that happens inside this function will be delivered to a [Component]'s scope and handled
 * by it.
 *
 * @param M incoming messages
 * @param S state of the application
 * @param C commands to be executed
 */
public typealias Updater<M, S, C> = (message: M, state: S) -> Update<S, C>

/**
 * Alias for kotlin's [Pair]. It can be created using the following [extensions][command]
 *
 * @param S state of the component
 * @param C commands to be executed. There's **NO GUARANTEE** of commands ordering, they can be
 * executed in any order. That implies calculation correctness mustn't depend on the ordering
 *
 * @param S state of the application
 * @param C commands to be executed
 */
public typealias Update<S, C> = Pair<S, Set<C>>

/**
 * Extension to combine state with command
 *
 * @receiver state to combine with command
 * @param S state to combine with command
 * @param C command
 * @param command command to combine with state
 * @return [Update] instance with given state and set that consists from a single command
 */
public infix fun <S, C> S.command(
    command: C,
): Update<S, C> = this to setOf(command)

/**
 * Extension to combine state with a single command provider
 *
 * @receiver state to combine with command
 * @param S state to combine with command
 * @param C command
 * @param command command to combine with state
 * @return [Update] instance with given state and set that consists from a single command
 */
public inline infix fun <S, C> S.command(
    command: S.() -> C,
): Update<S, C> = this to setOf(run(command))

/**
 * Extension to combine state with two commands
 *
 * @receiver state to combine with commands
 * @param S state to combine with command
 * @param C command
 * @param first the first command to combine with state
 * @param second the second command to combine with state
 * @return [Update] instance with given state and set of commands
 */
public fun <S, C> S.command(
    first: C,
    second: C,
): Update<S, C> = this to setOf(first, second)

/**
 * Extension to combine state with three commands
 *
 * @receiver state to combine with commands
 * @param S state to combine with command
 * @param C command
 * @param first the first command to combine with state
 * @param second the second command to combine with state
 * @param third the third command to combine with state
 * @return [Update] instance with given state and set of commands
 */
public fun <S, C> S.command(
    first: C,
    second: C,
    third: C,
): Update<S, C> =
    this to setOf(first, second, third)

/**
 * Extension to combine state with multiple commands
 *
 * @receiver state to combine with commands
 * @param S state to combine with command
 * @param C command
 * @param commands commands to combine with state
 * @return [Update] instance with given state and set of commands
 */
public fun <S, C> S.command(
    vararg commands: C,
): Update<S, C> = this command setOf(*commands)

/**
 * Extension to combine state with set of commands
 *
 * @receiver state to combine with commands
 * @param S state to combine with command
 * @param C command
 * @param commands commands to combine with state
 * @return [Update] instance with given state and set of commands
 */
public infix fun <S, C> S.command(
    commands: Set<C>,
): Update<S, C> = this to commands

/**
 * Extension to combine state with empty set of commands
 *
 * @receiver state to combine with commands
 * @param S state to combine with command
 * @return [Update] instance with given state and empty set of commands
 */
public fun <S> S.noCommand(): Update<S, Nothing> = this to setOf()

/**
 * Appends a command to an update
 *
 * @receiver update to append a command to
 * @param command command to append
 * @return [Update] instance with command appended
 */
public operator fun <S, C> Update<S, C>.plus(
    command: C,
): Update<S, C> {
    val (state, commands) = this

    return state command commands + command
}

/**
 * Appends commands to an update
 *
 * @receiver update to append commands to
 * @param additionalCommands commands to append
 * @return [Update] instance with commands appended
 */
public operator fun <S, C> Update<S, C>.plus(
    additionalCommands: Collection<C>,
): Update<S, C> {
    val (state, commands) = this

    return state command commands + additionalCommands
}
