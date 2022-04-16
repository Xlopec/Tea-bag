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

package com.oliynick.max.tea.core

/**
 * Snapshot is data structure that describes component's current state
 *
 * @param M message
 * @param S state
 * @param C command
 */
public sealed interface Snapshot<out M, out S, out C> {
    /**
     * Current state of a component
     */
    public val currentState: S

    /**
     * Set of commands to be resolved and executed
     */
    public val commands: Set<C>
}

/**
 * [Snapshot] that describes component's initial state
 *
 * @param S state
 * @param C command
 */
public data class Initial<out S, out C>(
    override val currentState: S,
    override val commands: Set<C>
) : Snapshot<Nothing, S, C>

/**
 * [Snapshot] that describes component's state
 *
 * @param M message
 * @param S state
 * @param C command
 */
public data class Regular<out M, out S, out C>(
    override val currentState: S,
    override val commands: Set<C>,
    /**
     * Previous state of a component
     */
    val previousState: S,
    /**
     * Message that triggered state update
     */
    val message: M
) : Snapshot<M, S, C>

/**
 * Extension to enable destructuring declaration on the [snapshot][Snapshot]
 */
public operator fun <S> Snapshot<*, S, *>.component1(): S = when (this) {
    is Initial -> currentState
    is Regular -> currentState
}

/**
 * Extension to enable destructuring declaration on the [snapshot][Snapshot]
 */
public operator fun <C> Snapshot<*, *, C>.component2(): Set<C> = when (this) {
    is Initial -> commands
    is Regular -> commands
}

/**
 * Extension to enable destructuring declaration on the [snapshot][Snapshot]
 */
public operator fun <S> Snapshot<*, S, *>.component3(): S? = when (this) {
    is Initial -> null
    is Regular -> previousState
}

/**
 * Extension to enable destructuring declaration on the [snapshot][Snapshot]
 */
public operator fun <M> Snapshot<M, *, *>.component4(): M? = when (this) {
    is Initial -> null
    is Regular -> message
}