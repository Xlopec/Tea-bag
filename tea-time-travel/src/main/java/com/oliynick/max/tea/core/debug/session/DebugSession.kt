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

package com.oliynick.max.tea.core.debug.session

import com.oliynick.max.tea.core.debug.protocol.NotifyServer
import kotlinx.coroutines.flow.Flow

/**
 * Debug session with ability to observe messages and states from
 * debug server and ability to send notifications
 *
 * @param M message type
 * @param S state type
 * @param J json tree type
 */
public interface DebugSession<M, S, J> {

    /**
     * Messages to be consumed by debuggable component
     */
    public val messages: Flow<M>

    /**
     * States that must replace any current state of
     * debuggable component
     */
    public val states: Flow<S>

    /**
     * Sends packet to debug server
     */
    public suspend operator fun invoke(
        packet: NotifyServer<J>
    )

}
