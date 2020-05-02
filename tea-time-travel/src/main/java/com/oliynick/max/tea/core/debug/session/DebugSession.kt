package com.oliynick.max.tea.core.debug.session

import kotlinx.coroutines.flow.Flow
import protocol.NotifyServer

/**
 * Debug session with ability to observe messages and states from
 * debug server and ability to send notifications
 *
 * @param M message type
 * @param S state type
 * @param J json tree type
 */
interface DebugSession<M, S, J> {

    /**
     * Messages to be consumed by debuggable component
     */
    val messages: Flow<M>

    /**
     * States that must replace any current state of
     * debuggable component
     */
    val states: Flow<S>

    /**
     * Sends packet to debug server
     */
    suspend operator fun invoke(
        packet: NotifyServer<J>
    )

}