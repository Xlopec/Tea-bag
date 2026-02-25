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

@file:Suppress("unused")

package io.github.xlopec.tea.time.travel.protocol

import kotlin.uuid.Uuid

/**
 * Message that notifies a debug server about component's state changes
 *
 * @param messageId message identifier
 * @param componentId component identifier
 * @param payload payload that tells a debug server how to process this message
 * @param J json specific implementation
 */
public data class NotifyServer<out J>(
    val messageId: Uuid,
    val componentId: ComponentId,
    val payload: ServerMessage<J>,
)

/**
 * Message that tells a client to apply changes
 *
 * @param id message identifier
 * @param component component identifier
 * @param message payload that tells client how to process this message
 * @param J json specific implementation
 */
public data class NotifyClient<out J>(
    val id: Uuid,
    val component: ComponentId,
    val message: ClientMessage<J>,
)
