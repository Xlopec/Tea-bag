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

package io.github.xlopec.tea.core.debug.protocol

/**
 * Represents messages that should be applied by client
 *
 * @param J implementation specific json type
 */
public sealed interface ClientMessage<out J>

/**
 * Represents commands that tells client to apply debug [message] as if it was just
 * a regular message
 *
 * @param message message to consume
 * @param J implementation specific json type
 */
public data class ApplyMessage<out J>(
    val message: J,
) : ClientMessage<J>

/**
 * Represents commands that tells client to apply debug [state] as if it was just
 * a regular state
 *
 * @param state new application state
 * @param J implementation specific json type
 */
public data class ApplyState<J>(
    val state: J,
) : ClientMessage<J>
