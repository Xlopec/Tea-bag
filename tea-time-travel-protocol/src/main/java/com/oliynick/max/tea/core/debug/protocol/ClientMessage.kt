package com.oliynick.max.tea.core.debug.protocol

/**
 * Represents messages that should be applied by client
 *
 * @param J implementation specific json type
 */
sealed class ClientMessage<out J>

/**
 * Represents commands that tells client to apply debug [message] as if it was just
 * a regular message
 *
 * @param message message to consume
 * @param J implementation specific json type
 */
data class ApplyMessage<out J>(
    val message: J,
) : ClientMessage<J>()

/**
 * Represents commands that tells client to apply debug [state] as if it was just
 * a regular state
 *
 * @param state new application state
 * @param J implementation specific json type
 */
data class ApplyState<J>(
    val state: J,
) : ClientMessage<J>()
