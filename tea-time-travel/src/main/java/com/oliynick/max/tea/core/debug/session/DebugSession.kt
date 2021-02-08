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