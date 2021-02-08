/*
 * Copyright (C) 2021. Maksym Oliinyk.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.oliynick.max.tea.core

/**
 * Snapshot is data structure that describes component's current state
 *
 * @param M message
 * @param S state
 * @param C command
 */
public sealed class Snapshot<out M, out S, out C> {
    /**
     * Current state of a component
     */
    public abstract val currentState: S

    /**
     * Set of commands to be resolved and executed
     */
    public abstract val commands: Set<C>
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
) : Snapshot<Nothing, S, C>()

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
) : Snapshot<M, S, C>()
