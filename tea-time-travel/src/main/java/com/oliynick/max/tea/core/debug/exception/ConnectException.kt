package com.oliynick.max.tea.core.debug.exception

/**
 * Signals that some connection problem occurred
 */
class ConnectException(
    message: String,
    cause: Throwable
) : RuntimeException(message, cause)
