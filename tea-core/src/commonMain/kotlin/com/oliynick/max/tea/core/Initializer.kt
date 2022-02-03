/*
 * MIT License
 *
 * Copyright (c) 2021. Maksym Oliinyk.
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

@file:Suppress("FunctionName")

package com.oliynick.max.tea.core

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext

/**
 * Initializer is an **impure** function that computes [initial][Initial] snapshot
 *
 * @param S initial state of the application
 * @param C initial set of commands to be executed
 */
public typealias Initializer<S, C> = suspend () -> Initial<S, C>

/**
 * Constructs initializer using initial [state] and set of [commands]
 *
 * @param state initial state
 * @param commands initial set of commands
 */
public fun <S, C> Initializer(
    state: S,
    commands: Set<C> = emptySet()
): Initializer<S, C> = { Initial(state, commands) }

/**
 * Constructs initializer using initial [state] and array of [commands]
 *
 * @param S state
 * @param C command
 * @param state initial state
 * @param commands initial set of commands
 */
public fun <S, C> Initializer(
    state: S,
    vararg commands: C
): Initializer<S, C> = Initializer(state, setOf(*commands))

/**
 * Constructs initializer using [block] that will be run on specified [dispatcher]
 *
 * @param S state
 * @param C command
 * @param dispatcher coroutine dispatcher to run block on
 * @param block initializer block to run
 */
public fun <S, C> Initializer(
    dispatcher: CoroutineDispatcher,
    block: suspend CoroutineScope.() -> Initial<S, C>
): Initializer<S, C> = { withContext(dispatcher, block) }
