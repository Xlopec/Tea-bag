/*
 * Copyright (C) 2019 Maksym Oliinyk.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.oliynick.max.elm.core.component

/**
 * Alias for function that returns component's state as string
 *
 * @param M message
 * @param S state
 * @param C command
 */
typealias Formatter<M, S, C> = (message: M, prevState: S, newState: S, commands: Set<C>) -> String

/**
 * Creates logger that delegates logging to Android logger
 *
 * @param tag tag to be used
 * @param formatter function that accepts component's state and transforms it to a string representation. The default value is [simpleFormatter]
 * @param M message
 * @param S state
 * @param C command
 * @return ready to use [interceptor][Interceptor]
 */
inline fun <M : Any, C : Any, S : Any> androidLogger(tag: String, crossinline formatter: Formatter<M, S, C> = ::simpleFormatter): Interceptor<M, S, C> {
    return { message, prevState, newState, commands -> println("$tag: ${formatter(message, prevState, newState, commands)}") }
}

/**
 * Default state to string transformer
 *
 * @param message message that caused state transition
 * @param prevState previous state
 * @param newState new state
 * @param commands set of commands to execute
 * @param M message
 * @param S state
 * @param C command
 * @return ready to use [interceptor][Interceptor]
 */
fun <M : Any, C : Any, S : Any> simpleFormatter(message: M, prevState: S, newState: S, commands: Set<C>): String {
    return "performing transition from $prevState to $newState caused by $message " +
            if (commands.isEmpty()) "without executable commands" else "and executing commands ${commands.joinToString()}"
}