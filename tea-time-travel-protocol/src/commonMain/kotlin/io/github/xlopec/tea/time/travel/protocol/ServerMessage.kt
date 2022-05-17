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

package io.github.xlopec.tea.time.travel.protocol

/**
 * Represents messages that should be consumed by debug server
 *
 * @param J implementation specific json type
 */
public sealed interface ServerMessage<out J>

/**
 * Represents message that informs server about state changes
 *
 * @param message message that was applied to debug component
 * @param oldState old component's state
 * @param newState new component's state
 * @param commands commands that were calculated by component
 * @param J implementation specific json type
 */
public data class NotifyComponentSnapshot<out J>(
    val message: J,
    val oldState: J,
    val newState: J,
    val commands: Set<J>,
) : ServerMessage<J>

/**
 * Notifies that new component attached to debug server
 *
 * @param state current component's state
 * @param commands commands that were used to initialize component
 * @param J implementation specific json type
 */
public data class NotifyComponentAttached<out J>(
    val state: J,
    val commands: Set<J>,
) : ServerMessage<J>
